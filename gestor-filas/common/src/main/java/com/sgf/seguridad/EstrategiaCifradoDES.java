package com.sgf.seguridad;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EstrategiaCifradoDES implements IEncriptacionStrategy {

    private SecretKeySpec secretKey;

    public EstrategiaCifradoDES(String clave){
        try{
            byte[] key = clave.getBytes("UTF-8");
            key = java.util.Arrays.copyOf(key, 8); // DES requiere una clave de 8 bytes
            this.secretKey = new SecretKeySpec(key, "DES");
        } catch (Exception e) {
            throw new RuntimeException("Error al configurar la clave DES", e);
        }
    }

    @Override
    public String encriptar(String dato) {
        try{
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesEncriptados = cipher.doFinal(dato.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(bytesEncriptados);

        }catch (Exception e) {
            throw new RuntimeException("Error al encriptar el dato", e);
        }

    }

    @Override
    public String desencriptar(String datoEncriptado) {
        try{
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesDecodificados = Base64.getDecoder().decode(datoEncriptado);
            byte[] bytesDesencriptados = cipher.doFinal(bytesDecodificados);

            return new String(bytesDesencriptados, "UTF-8");
        }catch (Exception e) {
            throw new RuntimeException("Error al desencriptar el dato", e);
        }
    }
    

}
