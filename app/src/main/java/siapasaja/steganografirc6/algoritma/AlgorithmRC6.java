package siapasaja.steganografirc6.algoritma;

import java.util.Arrays;

public class AlgorithmRC6 {

    static int w = 32, r = 20;
    static int[] S;
    //static int Pw = 0xb7e15163, Qw = 0x9e3779b9;
    //static int Pw = 0xb7e1, Qw = 0x9e37;
    static int Pw = 0xb7e15163, Qw = 0x9e3779b9;

    // CODE TO CONVERT HEXADECIMAL NUMBERS IN STRING TO BYTE ARRAY
    public static byte[] hexStringToByteArray(String s) {
        int string_len = s.length();
        byte[] data = new byte[string_len / 2];
        for (int i = 0; i < string_len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // CODE TO CONVERT BYTE ARRAY TO HEX FORMAT
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    // KEY SCHEDULING ALGORITHM
    public int[] KeySchedule(byte[] key) {

        int[] S = new int[2 * r + 4];
        S[0] = Pw;

        int c = key.length / (w / 8);
        int[] L = bytestoWords(key, c);

        for (int i = 1; i < (2 * r + 4); i++) {
            S[i] = S[i - 1] + Qw;
        }

        int A, B, i, j;

        A = B = i = j = 0;

        int v = 3 * Math.max(c, (2 * r + 4));

        for (int s = 0; s < v; s++) {
            A = S[i] = rotateLeft((S[i] + A + B), 3);
            B = L[j] = rotateLeft(L[j] + A + B, A + B);
            i = (i + 1) % (2 * r + 4);
            j = (j + 1) % c;
        }

        return S;
    }

    // ENCRYPTION ALGORITHM
    public byte[] encryption(byte[] keySchArray) {

        int temp, t, u;
        int[] temp_data = new int[keySchArray.length / 4];

        for (int i = 0; i < temp_data.length; i++) {
            temp_data[i] = 0;
        }

        temp_data = convertBytetoInt(keySchArray, temp_data.length);

        int A, B, C, D;

        A = B = C = D = 0;

        A = temp_data[0];
        B = temp_data[1];
        C = temp_data[2];
        D = temp_data[3];

        B = B + S[0];
        D = D + S[1];

        int lgw = 5;

        byte[] outputArr = new byte[keySchArray.length];

        for (int i = 1; i <= r; i++) {

            t = rotateLeft(B * (2 * B + 1), lgw);
            u = rotateLeft(D * (2 * D + 1), lgw);
            A = rotateLeft(A ^ t, u) + S[2 * i];
            C = rotateLeft(C ^ u, t) + S[2 * i + 1];

            temp = A;
            A = B;
            B = C;
            C = D;
            D = temp;
        }

        A = A + S[2 * r + 2];
        C = C + S[2 * r + 3];

        temp_data[0] = A;
        temp_data[1] = B;
        temp_data[2] = C;
        temp_data[3] = D;

        outputArr = convertIntToByte(temp_data, keySchArray.length);

        return outputArr;
    }

    //DECRYPTION ALGORITHM
    public byte[] decryption(byte[] keySchArray) {

        int temp, t, u;
        int A, B, C, D;

        A = B = C = D = 0;
        int[] temp_data_decryption = new int[keySchArray.length / 4];

        for (int i = 0; i < temp_data_decryption.length; i++) {
            temp_data_decryption[i] = 0;
        }

        temp_data_decryption = convertBytetoInt(keySchArray, temp_data_decryption.length);

        A = temp_data_decryption[0];
        B = temp_data_decryption[1];
        C = temp_data_decryption[2];
        D = temp_data_decryption[3];

        C = C - S[2 * r + 3];
        A = A - S[2 * r + 2];

        int lgw = 5;

        byte[] outputArr = new byte[keySchArray.length];
        for (int i = r; i >= 1; i--) {
            temp = D;
            D = C;
            C = B;
            B = A;
            A = temp;

            u = rotateLeft(D * (2 * D + 1), lgw);
            t = rotateLeft(B * (2 * B + 1), lgw);
            C = rotateRight(C - S[2 * i + 1], t) ^ u;
            A = rotateRight(A - S[2 * i], u) ^ t;

        }
        D = D - S[1];
        B = B - S[0];

        temp_data_decryption[0] = A;
        temp_data_decryption[1] = B;
        temp_data_decryption[2] = C;
        temp_data_decryption[3] = D;

        outputArr = convertIntToByte(temp_data_decryption, keySchArray.length);

        return outputArr;
    }

    // CONVERT INT TO BYTE FORM
    public static byte[] convertIntToByte(int[] integerArray, int length) {
        byte[] int_to_byte = new byte[length];
        for (int i = 0; i < length; i++) {
            int_to_byte[i] = (byte) ((integerArray[i / 4] >>> (i % 4) * 8) & 0xff);
        }

        return int_to_byte;
    }

    // CONVERT BYTE TO INT FORM
    private static int[] convertBytetoInt(byte[] arr, int length) {
        int[] byte_to_int = new int[length];
        for (int j = 0; j < byte_to_int.length; j++) {
            byte_to_int[j] = 0;
        }

        int counter = 0;
        for (int i = 0; i < byte_to_int.length; i++) {
            byte_to_int[i] = ((arr[counter++] & 0xff))
                    | ((arr[counter++] & 0xff) << 8)
                    | ((arr[counter++] & 0xff) << 16)
                    | ((arr[counter++] & 0xff) << 24);
        }
        return byte_to_int;

    }

    // CONVERT BYTE TO WORDS
    private static int[] bytestoWords(byte[] userkey, int c) {
        int[] bytes_to_words = new int[c];
        for (int i = 0; i < bytes_to_words.length; i++) {
            bytes_to_words[i] = 0;
        }

        for (int i = 0, off = 0; i < c; i++) {
            bytes_to_words[i] = ((userkey[off++] & 0xFF)) | ((userkey[off++] & 0xFF) << 8)
                    | ((userkey[off++] & 0xFF) << 16) | ((userkey[off++] & 0xFF) << 24);
        }

        return bytes_to_words;
    }

    // ROTATE LEFT METHOD
    private static int rotateLeft(int val, int pas) {
        return (val << pas) | (val >>> (32 - pas));
    }

    //ROTATE RIGHT METHOD
    private static int rotateRight(int val, int pas) {
        return (val >>> pas) | (val << (32 - pas));
    }

    public String runEncrype(String textEncrype, String key) {

        int stringLength = (textEncrype.length() - ((textEncrype.length() / 16) * 16)) % 16;
        if (stringLength != 0) {
            for (int i = 0; i < 16 - stringLength; i++) {
                textEncrype = textEncrype + "0";
            }
        }

        String temp_key = "";

        for (int j = 0; j < key.toCharArray().length; j++) {
            temp_key = temp_key + String.format("%04x", (int) key.charAt(j)).substring(2, 4);
        }

        String[] text = new String[textEncrype.length() / 16];
        String chiper = "";

        for (int i = 0; i < textEncrype.length() / 16; i++) {
            text[i] = textEncrype.substring(i * 16, (i + 1) * 16);
            String temp_text = "";
            for (int j = 0; j < text[i].toCharArray().length; j++) {
                temp_text = temp_text + String.format("%04x", (int) text[i].charAt(j)).substring(2, 4);
            }

            byte[] keys = hexStringToByteArray(temp_key);
            System.out.println(Arrays.toString(keys));
            byte[] W = hexStringToByteArray(temp_text);
            System.out.println(Arrays.toString(W));
            S = new AlgorithmRC6().KeySchedule(keys);

            byte[] encrypt = new AlgorithmRC6().encryption(W);
            String encrypted_text = byteArrayToHex(encrypt);
            chiper = chiper + encrypted_text.replaceAll("..", "$0 ");
        }
        return chiper;
    }

    public String runDecrype(String chiper, String key) {
        String chiperArray[] = new String[chiper.length() / 32];

        chiper = chiper.replace(" ", "");

        for (int i = 0; i < chiper.length() / 32; i++) {
            chiperArray[i] = chiper.substring(i * 32, (i + 1) * 32);
        }

        String temp_key = "";

        for (int j = 0; j < key.toCharArray().length; j++) {
            temp_key = temp_key + String.format("%04x", (int) key.charAt(j)).substring(2, 4);
        }

        String decrypted_text = "";
        for (int i = 0; i < chiperArray.length; i++) {
            byte[] key2 = hexStringToByteArray(temp_key);
            byte[] X = hexStringToByteArray(chiperArray[i]);
            S = new AlgorithmRC6().KeySchedule(key2);
            byte[] decrypta = new AlgorithmRC6().decryption(X);

            for (int j = 0; j < decrypta.length; j++) {
                if (decrypta[j] != (byte) 0) {
                    decrypted_text = decrypted_text + ((char) decrypta[j]);
                }
            }
        }

        return decrypted_text;
    }
}
