package com.huifeng.entity;

/**
 * role com.huifeng.entity
 */
public class Role {
    private String roleName;

    public Role(String roleName) {
        this.roleName = roleName;
    }

    /**
     * get the roleName
     *
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * set the roleName
     *
     * @param roleName the roleName
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
