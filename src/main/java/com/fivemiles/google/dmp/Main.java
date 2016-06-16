package com.fivemiles.google.dmp;

import com.beust.jcommander.JCommander;
import com.fivemiles.google.dmp.CookieBulkUpload.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by ying on 19/4/16.
 */
public class Main {
  private static Logger logger = LogManager.getLogger("ProcessLog");
  private static Logger loggerErrorFormat = LogManager.getLogger("Error.Format");
  private static Logger loggerErrorResponse = LogManager.getLogger("Error.Response");

  private static final String SEPARATOR = ",";

  private static final String REQUEST_URL = "https://cm.g.doubleclick.net/upload?nid=";
  // Gzip of data shorter than this probably won't be worthwhile
  private static long DEFAULT_SYNC_MIN_GZIP_BYTES = 256;

  // counter
  private static enum CounterType { TOTAL, SKIPPED, ERR_LEN_NOT_36, ERR_NO_LETTER, ERR_UNKNOWN, REQUEST_SENT, ERR_RESPONE };
  private static EnumMap<CounterType, Integer> counter = new EnumMap<CounterType, Integer>(CounterType.class);

  private static void addCounter(CounterType countType){
    addCounter(countType, 1);
  }
  private static void addCounter(CounterType countType, int count){
    Integer oldValue = counter.get(countType);
    if (oldValue == null) {
      oldValue = 0;
    }
    counter.put(countType, oldValue + count);
  }

  public static void main(String[] argv) throws IOException {
    // parse command line parameters
    Args args = new Args();
    JCommander jCommander = new JCommander(args, argv);
    jCommander.setProgramName("Bulk Upload");
    if (args.help) {
      jCommander.usage();
      System.exit(-1);
    }

    final String fileName = args.file;
    final Long userListId = args.userListId;
    final int UPLOAD_BATCH_COUNT = args.batchSize;
    final String skip = args.skip;

    // read file
    BufferedReader stream = null;
    try {
      stream = new BufferedReader(new FileReader(fileName));

      List<UserDataOperation> opList = new ArrayList<UserDataOperation>(UPLOAD_BATCH_COUNT);
      String line;

      logger.info("--------------------------------------------------------------------------------------");
      logger.info("Begin processing...");
      logger.info("File name: {}", fileName);
      logger.info("User list id: {}", userListId);
      logger.info("Batch size: {}", UPLOAD_BATCH_COUNT);
      logger.info("Skip: {}", skip);
      logger.info("--------------------------------------------------------------------------------------");

      while ((line = stream.readLine()) != null) {
        addCounter(CounterType.TOTAL);

        // process a single line
        String[] arr = line.split(SEPARATOR);
        UserDataOperation op = null;

        // skip length != 36
        if (arr[0].length() != 36) {
          addCounter(CounterType.ERR_LEN_NOT_36);
          loggerErrorFormat.error("{}, {}", arr[0], "ERR_LEN_NOT_36");
          continue;
        }

        if (arr[0].toLowerCase().equals(arr[0])
            && arr[0].toUpperCase().equals(arr[0])) {
          addCounter(CounterType.ERR_NO_LETTER);
          loggerErrorFormat.error("{}, {}", arr[0], "ERR_NO_LETTER");
          continue;
        }

        // all lower case means android, upper case means ios
        if (arr[0].toLowerCase().equals(arr[0])) {
          if (skip.equalsIgnoreCase("android")) {
            addCounter(CounterType.SKIPPED);
            continue;
          }
          op = UserDataOperation.newBuilder()
              .setUserId(arr[0])
              .setUserListId(userListId)
              .setUserIdType(UserIdType.ANDROID_ADVERTISING_ID)
              .build();
        } else if (arr[0].toUpperCase().equals(arr[0])) {
          if (skip.equalsIgnoreCase("ios")) {
            addCounter(CounterType.SKIPPED);
            continue;
          }
          op = UserDataOperation.newBuilder()
              .setUserId(arr[0])
              .setUserListId(userListId)
              .setUserIdType(UserIdType.IDFA)
              .build();
        }
        if (op == null) {
          // don't know why we're here, just record
          addCounter(CounterType.ERR_UNKNOWN);
          continue;
        }

        opList.add(op);

        if (opList.size() >= UPLOAD_BATCH_COUNT) {
          // send a batch
          sendRequest(opList);
        }
      }
      if (opList.size() > 0) {
        // send last batch
        sendRequest(opList);
      }
    } finally {
      if (stream != null) {
        stream.close();
      }
      // final report
      logger.info("--------------------------------------------------------------------------------------");
      for(Map.Entry<CounterType, Integer> entry : counter.entrySet()) {
        logger.info("{}: {}", entry.getKey(), entry.getValue());
      }
      logger.info("SUCCESS: {}", counter.get(CounterType.REQUEST_SENT) - counter.get(CounterType.ERR_RESPONE));
      logger.info("--------------------------------------------------------------------------------------");
    }
  }

  private static void sendRequest(List<UserDataOperation> opList) throws IOException {
    // build data request
    UpdateUsersDataRequest dataRequest = UpdateUsersDataRequest.newBuilder()
        .addAllOps(opList).build();

    // send HTTP post request
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(REQUEST_URL);
    ByteArrayEntity reqEntity = (ByteArrayEntity)getCompressedEntity(dataRequest.toByteArray());
    post.setEntity(reqEntity);

    HttpResponse response = httpClient.execute(post);
    addCounter(CounterType.REQUEST_SENT, opList.size());

    if (response.getStatusLine().getStatusCode() != 200) {
      // response error
      addCounter(CounterType.ERR_RESPONE, opList.size());
      logger.info("Batch size: {}, HTTP error code: {}, error count: {}",
          opList.size(), response.getStatusLine().getStatusCode(), opList.size());
    } else {
      // get response
      HttpEntity responseEntity = response.getEntity();
      UpdateUsersDataResponse dataResponse = UpdateUsersDataResponse.parseFrom(responseEntity.getContent());

      addCounter(CounterType.ERR_RESPONE, dataResponse.getErrorsCount());

      logger.info("Batch size: {}, status: {}, error count: {}", opList.size(), dataResponse.getStatus(), dataResponse.getErrorsCount());
      for (ErrorInfo error : dataResponse.getErrorsList()) {
        loggerErrorResponse.error("{}, {}", error.getUserId(), error.getErrorCode());
      }
    }

    // clear operation list
    opList.clear();
  }

  private static AbstractHttpEntity getCompressedEntity(byte data[])
      throws IOException {
    AbstractHttpEntity entity;

    if (data.length < DEFAULT_SYNC_MIN_GZIP_BYTES) {
      entity = new ByteArrayEntity(data);
    } else {
      ByteArrayOutputStream arr = new ByteArrayOutputStream();
      OutputStream zipper = new GZIPOutputStream(arr);
      zipper.write(data);
      zipper.close();
      entity = new ByteArrayEntity(arr.toByteArray());
      entity.setContentEncoding("gzip");
    }

    entity.setContentType("application/octet-stream");
    return entity;
  }

}
