package com.upcomingmovies;

import com.upcomingmovies.cli.MovieCliCommand;
import picocli.CommandLine;

public final class Main {

  private Main() {}

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(new MovieCliCommand());
    cmd.setCaseInsensitiveEnumValuesAllowed(true);
    int exitCode = cmd.execute(args);
    System.exit(exitCode);
  }
}
