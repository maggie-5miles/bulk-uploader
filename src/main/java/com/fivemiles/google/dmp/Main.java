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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created by ying on 19/4/16.
 */
public class Main {

  private static final int UPLOAD_BATCH_COUNT = 10;
  private static final String SEPARATOR = ",";

  private static final String REQUEST_URL = "https://cm.g.doubleclick.net/upload?nid=";
  // Gzip of data shorter than this probably won't be worthwhile
  private static long DEFAULT_SYNC_MIN_GZIP_BYTES = 256;

  public static void main(String[] argv) throws IOException {
    // parse command line parameters
    Args args = new Args();
    JCommander jCommander = new JCommander(args, argv);
    jCommander.setProgramName("Bulk Upload");
    if (args.help) {
      jCommander.usage();
      System.exit(-1);
    }

    String fileName = args.file;
    Long userListId = args.userListId;

    // read file
    BufferedReader stream = null;
    try {
      stream = new BufferedReader(new FileReader(fileName));

      List<UserDataOperation> opList = new ArrayList<UserDataOperation>(UPLOAD_BATCH_COUNT);
      String line;
      int totalCount = 0;

      System.out.println("begin processing...");
      while ((line = stream.readLine()) != null) {
        // process a single line
        String[] arr = line.split(SEPARATOR);
        UserDataOperation op = UserDataOperation.newBuilder()
            .setUserId(arr[0])
            .setUserListId(userListId)
            .setUserIdType(UserIdType.GOOGLE_USER_ID)
            .build();
        opList.add(op);
        ++totalCount;
        if (opList.size() >= UPLOAD_BATCH_COUNT) {
          // send a batch
          sendRequest(opList);
          System.out.printf("%d sent. \r\n", totalCount);
        }
      }
      if (opList.size() > 0) {
        // send last batch
        sendRequest(opList);
        System.out.printf("%d sent. \r\n", totalCount);
      }
    } finally {
      if (stream != null) {
        stream.close();
      }
      System.out.println("done.");
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
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new RuntimeException("Failed : HTTP error code : "
          + response.getStatusLine().getStatusCode());
    }

    // get response
    HttpEntity responseEntity = response.getEntity();
    UpdateUsersDataResponse dataResponse = UpdateUsersDataResponse.parseFrom(responseEntity.getContent());

    System.out.printf("-- Response.status = %s. \r\n", dataResponse.getStatus());
    System.out.printf("-- Response.errorCount = %d. \r\n", dataResponse.getErrorsCount());
    System.out.printf("-- Response.errors = %s. \r\n", dataResponse.getErrorsList().toString());

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
