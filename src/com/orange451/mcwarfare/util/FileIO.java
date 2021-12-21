package com.orange451.mcwarfare.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class FileIO {
	public static BufferedReader file_text_open_read(String str) {
		try {
			return new BufferedReader(new FileReader(str));
		} catch (Exception e) {
			//
		}
		return null;
	}

	public static String file_text_read_line(BufferedReader out) {
		try {
			return out.readLine();
		} catch (Exception localException) {
			//
		}
		return null;
	}

	public static BufferedWriter file_text_open_write(String str) {
		try {
			return new BufferedWriter(new FileWriter(str));
		} catch (Exception e) {
			//
		}
		return null;
	}

	public static boolean file_text_write_line(BufferedWriter out, String str) {
		try {
			out.write(str);
			out.newLine();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void file_text_close(BufferedWriter out) {
		try {
			out.flush();
		} catch (Exception localException1) {
			//
		}
		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void file_text_close(BufferedReader out) {
		try {
			out.close();
		} catch (Exception localException) {
			//
		}
	}
}