package com.xs.chat.Utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class StringUtil {
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = (int) (Math.random() * 36);
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
   public static boolean isEmpty(String str){
       if (str==null||str.equals("")||"\u0000".equals(str)){
           return true;
       }else if ("".equals(str.trim())){
           return true;
       }else return false;
   }

   public static String generateUUID(){
        return IdUtil.fastSimpleUUID();
   }

   public static String encodeHash(String str){
        return BCrypt.hashpw(str, BCrypt.gensalt());
   }
   public static boolean verifyHash(String str,String hash){
        return BCrypt.checkpw(str, hash);
   }
}
