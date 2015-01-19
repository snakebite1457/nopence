package org.meyerlab.nopence.gov_browser_parser.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * @author Dennis Meyer
 */
public class WriteToDB {

    private static JsonParser _jsonParser;

    public static void write() throws DirNotValidException {
        Connection c = null;
        try {
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/govData");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");



        _jsonParser = new JsonParser();

        File dir = new File(GovOption.InputDir);

        if (!Helper.isDirValid(dir)) {
            throw new DirNotValidException(String.format(Constants
                    .EX_DIR_NOT_VALID, GovOption.InputDir));
        }

        FileHelper fileHelper = new FileHelper();
        ParsedFileHelper parsedFileHelper = new ParsedFileHelper();

        try {
            for (File dataFile : dir.listFiles()) {
                fileHelper.reset(dataFile, true);

                System.out.println("File: " + dataFile);


                String line = fileHelper.nextLine();
                 while (line != null) {
                    try {
                        if (_jsonParser.parse(line).isJsonObject()) {
                            JsonObject jsonGovObj =
                                    _jsonParser.parse(line).getAsJsonObject();

                            if (!jsonGovObj.has("a")) {
                                line = fileHelper.nextLine();
                                continue;
                            }


                            String sql = "INSERT INTO rawgovdata(\n" +
                                    "user_agent, \n" +
                                    "country_code, \n" +
                                    "known_user,\n" +
                                    "global_bitly_hash, \n" +
                                    "encoding_user_bitly_hash, \n" +
                                    "encoding_user_login, \n" +
                                    "short_url_cname, \n" +
                                    "referring_url, \n" +
                                    "long_url, \n" +
                                    "timesta,\n" +
                                    "geo_region, \n" +
                                    "latitude, \n" +
                                    "longitude, \n" +
                                    "geo_city_name,\n" +
                                    "timezone,\n" +
                                    "timestap_hash, \n" +
                                    "accept_language)\n" +
                                    "    VALUES (?, ?, ?, \n" +
                                    "            ?, ?, ?, ?, ?, \n" +
                                    "            ?, ?, ?, ?, ?, \n" +
                                    "            ?, ?, ?, ?);\n";

                            PreparedStatement stmt = c.prepareStatement(sql);
                            if (jsonGovObj.has("a")) {
                                stmt.setString(1, jsonGovObj.get("a").getAsString());
                            } else {
                                stmt.setString(1, null);
                            }
                            if (jsonGovObj.has("c") && !jsonGovObj.get("c").isJsonNull()) {
                                stmt.setString(2, jsonGovObj.get("c").getAsString());
                            } else {
                                stmt.setString(2, null);
                            }

                            if (jsonGovObj.has("nk")&& !jsonGovObj.get("nk").isJsonNull()) {
                                stmt.setString(3, jsonGovObj.get("nk").getAsString());
                            } else {
                                stmt.setString(3, null);
                            }
                            if (jsonGovObj.has("g")&& !jsonGovObj.get("g").isJsonNull()) {
                                stmt.setString(4, jsonGovObj.get("g").getAsString());
                            } else {
                                stmt.setString(4, null);
                            }
                            if (jsonGovObj.has("h")&& !jsonGovObj.get("h").isJsonNull()) {
                                stmt.setString(5, jsonGovObj.get("h").getAsString());
                            } else {
                                stmt.setString(5, null);
                            }
                            if (jsonGovObj.has("l")&& !jsonGovObj.get("l").isJsonNull()) {
                                stmt.setString(6, jsonGovObj.get("l").getAsString());
                            }else {
                                stmt.setString(6, null);
                            }
                            if (jsonGovObj.has("hh")&& !jsonGovObj.get("hh").isJsonNull()) {
                                stmt.setString(7, jsonGovObj.get("hh").getAsString());
                            }else {
                                stmt.setString(7, null);
                            }
                            if (jsonGovObj.has("r")&& !jsonGovObj.get("r").isJsonNull()) {
                                stmt.setString(8, jsonGovObj.get("r").getAsString());
                            }else {
                                stmt.setString(8, null);
                            }

                            if (jsonGovObj.has("u")&& !jsonGovObj.get("u").isJsonNull()) {
                                stmt.setString(9, jsonGovObj.get("u").getAsString());
                            }else {
                                stmt.setString(9, null);
                            }

                            if (jsonGovObj.has("t")&& !jsonGovObj.get("t").isJsonNull()) {
                                stmt.setTimestamp(10, new Timestamp
                                        (jsonGovObj.get("t").getAsLong() *
                                                1000L));
                            }else {
                                stmt.setTimestamp(10, null);
                            }

                            if (jsonGovObj.has("gr")&& !jsonGovObj.get("gr").isJsonNull()) {
                                stmt.setString(11, jsonGovObj.get("gr").getAsString());
                            }else {
                                stmt.setString(11, null);
                            }

                            if (jsonGovObj.has("ll")&& !jsonGovObj.get("ll").isJsonNull()) {
                                stmt.setDouble(12, jsonGovObj.get("ll").getAsJsonArray().get(0).getAsDouble());
                                stmt.setDouble(13, jsonGovObj.get("ll").getAsJsonArray().get(1).getAsDouble());
                            }else {
                                stmt.setTimestamp(12, null);
                                stmt.setTimestamp(13, null);
                            }

                            if (jsonGovObj.has("cy")&& !jsonGovObj.get("cy").isJsonNull()) {
                                stmt.setString(14, jsonGovObj.get("cy").getAsString());
                            }else {
                                stmt.setString(14, null);
                            }

                            if (jsonGovObj.has("tz")&& !jsonGovObj.get("tz").isJsonNull()) {
                                stmt.setString(15, jsonGovObj.get("tz").getAsString());
                            }else {
                                stmt.setString(15, null);
                            }

                            if (jsonGovObj.has("hc")&& !jsonGovObj.get("hc").isJsonNull()) {
                                stmt.setTimestamp(16,new Timestamp(jsonGovObj
                                        .get("hc").getAsLong() * 1000L));
                            }else {
                                stmt.setTimestamp(16, null);
                            }
                            if (jsonGovObj.has("al")&& !jsonGovObj.get("al").isJsonNull()) {
                                stmt.setString(17, jsonGovObj.get("al").getAsString());
                            }else {
                                stmt.setString(17, null);
                            }

                            stmt.executeUpdate();
                        }
                    } catch (JsonSyntaxException ex) { } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    line = fileHelper.nextLine();
                }
                System.out.println("File done");
            }
        } catch (FileNotValidException | IOException e) {
            e.printStackTrace();
        } finally {
            parsedFileHelper.emptyBuffer();
        }

        System.out.print("ready");
    }

}
