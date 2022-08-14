package com.pgmmers.radar.util;

import org.apache.commons.lang.RandomStringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * <p>MD5Utils class.</p>
 *
 * @author linguibao
 */
public class MD5Utils {

	private static final String[] SALEARR = { "q", "a", "z", "w", "s", "x",
			"e", "d", "c", "r", "f", "v", "t", "g", "b", "y", "h", "n", "u",
			"j", "m", "i", "k", "o", "l", "p", "_" };

	static char[] chars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'h', 'i', 'o', 't',
			'y', 'E', 'D', 'O', 'T', 'Y', 'j', 'p', 'u', 'z', 'F', 'J', 'P',
			'U', 'Z', 'k', 'q', 'v', 'A', 'G', 'K', 'Q', 'V', 'l', 'r', 'w',
			'B', 'H', 'L', 'R', 'W', 'm', 's', 'x', 'C', 'I', 'M', 'S', 'X' };

	/**
	 * 生成盐值
	 *
	 * @return a {@link String} object.
	 */
	public static String getSalt() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < 6; i++) {
			sb.append(SALEARR[random.nextInt(26)]);
		}
		return sb.toString();
	}

	/**
	 * <p>getTempPwd.</p>
	 *
	 * @return a {@link String} object.
	 */
	public static String getTempPwd() {
		return RandomStringUtils.random(10, chars);
	}

	/**
	 * MD5加密
	 *
	 * @param str a {@link String} object.
	 * @param salt a {@link String} object.
	 * @return a {@link String} object.
	 */
	public static String encrypt(String str, String salt) {
		MessageDigest messageDigest = null;
		str += salt;
		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		byte[] byteArray = messageDigest.digest();

		StringBuilder md5StrBuff = new StringBuilder();

		for (int i = 0; i < byteArray.length; i++) {
			String hexString = Integer.toHexString(0xFF & byteArray[i]);
			if (hexString.length() == 1) {
				md5StrBuff.append("0").append(hexString);
			}
			else {
				md5StrBuff.append(hexString);
			}
		}
		return md5StrBuff.toString();
	}

}
