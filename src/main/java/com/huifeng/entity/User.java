package com.huifeng.entity;

/**
 * user com.huifeng.entity
 */
public class User {
    private String userName;
    private String passWord;

    public User(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;

    }

    /**
     * get userName
     *
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * set userName
     *
     * @param userName the userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * get passWord
     *
     * @return the passWord
     */
    public String getPassWord() {
        return passWord;
    }

    /**
     * set the passWord
     *
     * @param passWord the passWord
     */
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
