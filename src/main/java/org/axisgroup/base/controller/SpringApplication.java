package org.axisgroup.base.controller;


import java.io.IOException;

import com.paypal.base.codec.binary.Base64;




public class SpringApplication{

    public static void main(String args[]) throws IOException {
        String orig = "62";

        //encoding  byte array into base 64
        byte[] encoded = Base64.encodeBase64(orig.getBytes());     
      
        System.out.println("Original String: " + orig );
        System.out.println("Base64 Encoded String : " + new String(encoded));
      
        //decoding byte array into base64
        byte[] decoded = Base64.decodeBase64(encoded);      
        System.out.println("Base 64 Decoded  String : " + new String(decoded));

    }
}


