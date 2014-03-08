package com.example.util;

/**
 * Created by gsl on 14-3-7.
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;


public class Helper {
    public static String filetostring(String filename){
        StringBuffer sb = new StringBuffer();
        String charset;
        Object log;
        String file;
        try {
            LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("unsupport encoding");
        } catch (FileNotFoundException e) {
            System.out.println("file not fount");
        } catch (IOException e) {
            System.out.println("io exception");
        }
        return sb.toString();
    }
}
