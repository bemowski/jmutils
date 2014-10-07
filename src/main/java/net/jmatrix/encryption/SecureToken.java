package net.jmatrix.encryption;

/** 
 * 
 */
public class SecureToken {
    /** Encrypted token string. **/
    String tokenString=null;

    public void setTokenString(String s) {
        this.tokenString = s;
    }

    public String getTokenString() {
        return this.tokenString;
    }
}
