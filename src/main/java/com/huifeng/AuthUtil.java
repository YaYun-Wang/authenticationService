package com.huifeng;

import com.huifeng.constant.Constant;
import com.huifeng.entity.Role;
import com.huifeng.entity.User;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * the service for authentication
 */
public class AuthUtil {

    private static Map<String, Set<String>> USER_ROLE_MAP = new HashMap<String, Set<String>>(); // users-roles map
    private static Map<String, String> USERS_MAP = new HashMap<String, String>(); // users map(userName,passWord)
    private static Set<String> ROLE_SETS = new HashSet<String>(); // role map
    private static Set<String> AUTHTOKEN_SET = new HashSet<>(); // autoTokens

    /**
     * create user
     *
     * @param userName the userName
     * @param passWord the passWord
     * @return is create user successfully
     */
    public static boolean createUser(String userName, String passWord) {

        if (USERS_MAP.containsKey(userName)) {
            return false;
        }

        USERS_MAP.put(userName, getEncryptPassWord(passWord));
        return true;
    }


    public static boolean deleteUser(User user) {
        String userName = user.getUserName();

        // must have correct userName and passWord
        if (USERS_MAP.containsKey(userName) && getEncryptPassWord(user.getPassWord()).equals(USERS_MAP.get(userName))) {
            USERS_MAP.remove(userName);
            USER_ROLE_MAP.remove(userName);
            return true;
        }
        return false;
    }

    /**
     * create role
     *
     * @param roleName the roleName
     * @return create successfully or not
     */
    public static boolean createRole(String roleName) {
        if (ROLE_SETS.contains(roleName)) {
            return false;
        }
        ROLE_SETS.add(roleName);
        return true;
    }

    /**
     * delete the role
     *
     * @param role the role
     * @return delete successfully or not
     */
    public static boolean deleteRole(Role role) {
        String roleName = role.getRoleName();
        if (!ROLE_SETS.contains(roleName)) {
            return false;
        }

        // remove the role from ROLE_SETS
        ROLE_SETS.remove(roleName);

        // remove the role from USER_ROLE_MAP
        Iterator<Map.Entry<String, Set<String>>> it = USER_ROLE_MAP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Set<String>> entry = it.next();
            Set<String> roles = entry.getValue();

            if (roles.contains(roleName)) {
                roles.remove(roleName);
            }

            entry.setValue(roles);
        }
        return true;
    }

    /**
     * add role to user
     *
     * @param user the user
     * @param role the role
     * @return add successfully or not
     */
    public static boolean addRoleToUser(User user, Role role) {
        if (!(checkUser(user) && checkRole(role))) {
            return false;
        }

        String userName = user.getUserName();
        String roleName = role.getRoleName();
        Set<String> roles = USER_ROLE_MAP.getOrDefault(userName, new HashSet<String>());
        if (!roles.contains(roleName)) {
            roles.add(roleName);
            USER_ROLE_MAP.put(userName, roles);
        }

        return true;
    }


    /**
     * get the auth token
     *
     * @param userName the userName
     * @param passWord the passWord
     * @return the authToken,return error when user is invalid
     */
    public static String authenticate(String userName, String passWord) {
        User user = new User(userName, passWord);
        if (!checkUser(user)) {
            return Constant.ERROR;
        }

        String authToken = getAuthToken(user);
        AUTHTOKEN_SET.add(authToken);
        return authToken;
    }

    /**
     * invalidate authToken
     *
     * @param authToken the authToken
     * @return invalid successfully or not
     */
    public static boolean invalidate(String authToken) {
        if (!checkAuthToken(authToken)) {
            return false;
        }

        AUTHTOKEN_SET.remove(authToken);
        return true;
    }


    /**
     * get all roles od user
     *
     * @param authToken the authToken
     * @return the all roles of user,return error when authToken is invalid
     */
    public static Set<String> getAllRoles(String authToken) {
        if (!checkAuthToken(authToken)) {
            return Sets.newHashSet(Constant.ERROR);
        }
        String[] array = authToken.split(Constant.SEPARATOR);
        return USER_ROLE_MAP.getOrDefault(array[0], new HashSet<>());

    }

    /**
     * check the role of user is valid or not
     *
     * @param authToken the authToken
     * @param role      the role
     * @return is valid or not
     */
    public static String checkRole(String authToken, Role role) {
        if (!checkAuthToken(authToken)) {
            return Constant.ERROR;
        }

        String[] array = authToken.split(Constant.SEPARATOR);
        String userName = array[0];

        if (USER_ROLE_MAP.containsKey(userName) && USER_ROLE_MAP.get(userName).contains(role.getRoleName())) {
            return Constant.TRUE;
        }
        return Constant.FALSE;
    }

    /**
     * encrypt passWord
     *
     * @param passWord the passWord
     * @return the encrypt passWord
     */
    private static String getEncryptPassWord(String passWord) {
        return String.valueOf(passWord.hashCode());
    }

    /**
     * check user is valid or not
     *
     * @param user the user
     * @return is valid or not
     */
    private static boolean checkUser(User user) {
        String userName = user.getUserName();

        if (USERS_MAP.containsKey(userName) && getEncryptPassWord(user.getPassWord()).equals(USERS_MAP.get(userName))) {
            return true;
        }
        return false;

    }

    /**
     * check role is valid or not
     *
     * @param role the role
     * @return the role has created or not
     */
    private static boolean checkRole(Role role) {
        return ROLE_SETS.contains(role.getRoleName());
    }

    /**
     * get authToken by userName and passWord
     *
     * @param user the user
     * @return the authToken, contains userName, encrypt passWord, and timeValue
     */
    private static String getAuthToken(User user) {
        String userName = user.getUserName();
        String passWord = user.getPassWord();
        StringBuilder sb = new StringBuilder();
        long timeValue = new Date().getTime();
        sb.append(userName).append(Constant.SEPARATOR).append(getEncryptPassWord(passWord)).append(Constant.SEPARATOR).append(timeValue);
        return sb.toString();
    }

    /**
     * check auth Token is valid or not
     *
     * @param authToken the authToken
     * @return authToken is valid or not
     */
    private static boolean checkAuthToken(String authToken) {
        if (!AUTHTOKEN_SET.contains(authToken)) {
            return false;
        }
        String[] array = authToken.split(Constant.SEPARATOR);
        if (array.length != 3) {
            return false;
        }
        String userName = array[0];
        String encryptPassWord = array[1];
        boolean userExists = USERS_MAP.containsKey(userName) && encryptPassWord.equals(USERS_MAP.get(userName));
        return userExists && checkAuthTokenWithInPeriod(authToken);
    }

    /**
     * check the authToken is expired or not
     *
     * @param authToken the authToken
     * @return is expired or not
     */
    private static boolean checkAuthTokenWithInPeriod(String authToken) {
        String[] array = authToken.split(Constant.SEPARATOR);
        Long oldTimeValue = Long.valueOf(array[2]);
        Long nowTimeValue = new Date().getTime();
        return nowTimeValue - oldTimeValue < Constant.EXPIRED_PERIOD;
    }

}

