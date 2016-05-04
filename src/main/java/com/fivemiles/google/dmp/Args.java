package com.fivemiles.google.dmp;

import com.beust.jcommander.Parameter;

/**
 * Created by ying on 19/4/16.
 */
public class Args {
  @Parameter(names = { "--input-file", "-i" }, required = true, description = "The input file")
  String file;

  @Parameter(names = { "--user-list-id", "-l" }, required = true, description = "The user list id")
  Long userListId;

  @Parameter(names = { "--help", "-h" }, help = true, description = "Display help information")
  boolean help = false;
}
