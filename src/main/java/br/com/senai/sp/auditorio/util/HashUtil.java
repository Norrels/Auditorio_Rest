package br.com.senai.sp.auditorio.util;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

public class HashUtil {
	public static String hash(String palavra) {
		String salt = "f1lh0sDeJ3su2";
		palavra = salt + palavra;
		String hash = Hashing.sha384().hashString(palavra, StandardCharsets.UTF_8).toString();
		return hash;
	}
}
