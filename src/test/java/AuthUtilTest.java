import com.huifeng.AuthUtil;
import com.huifeng.constant.Constant;
import com.huifeng.entity.Role;
import com.huifeng.entity.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Tester for AuthUtil
 */
public class AuthUtilTest {
    /**
     * bedore set
     */
    @Before
    public void before() {
        AuthUtil.createUser("user0", "pass0");
        AuthUtil.createUser("user1", "pass1");

        AuthUtil.createRole("role0");
        AuthUtil.createRole("role1");

        User user0 = new User("user0", "pass0");
        User user1 = new User("user1", "pass1");

        Role role0 = new Role("role0");
        Role role1 = new Role("role1");

        // add role0 to user0
        AuthUtil.addRoleToUser(user0, role0);

        // add role1 to user1
        AuthUtil.addRoleToUser(user1, role1);

        AuthUtil.authenticate("user0", "pass0");
    }


    /**
     * test createUser
     */
    @Test
    public void testCreateUser() {
        // create a new user
        Assert.assertTrue(AuthUtil.createUser("user2", "pass2"));

        // create a exists user
        Assert.assertFalse(AuthUtil.createUser("user0", "pass0"));
    }

    /**
     * test deleteUser
     */
    @Test
    public void testDeleteUser() {
        // correct userName, correct passWord
        User user = new User("user0", "pass0");
        Assert.assertTrue(AuthUtil.deleteUser(user));

        Assert.assertFalse(AuthUtil.deleteUser(user));

        // correct userName, wrong passWord
        user = new User("user1", "pass0");
        Assert.assertFalse(AuthUtil.deleteUser(user));

    }

    /**
     * test create role
     */
    @Test
    public void testCreateRole() {
        // create a new role
        Assert.assertTrue(AuthUtil.createRole("role2"));

        // create a exists role
        Assert.assertFalse(AuthUtil.createRole("role0"));

    }

    /**
     * test deleteRole
     */
    @Test
    public void testDeleteRole() {
        // delete a not exists role
        Assert.assertFalse(AuthUtil.deleteRole(new Role("role3")));

        // delete a exists role
        Assert.assertTrue(AuthUtil.deleteRole(new Role("role0")));

    }

    /**
     * test add role to user
     */
    @Test
    public void testAddRoleToUser() {
        // get authToken of user0
        String authToken = AuthUtil.authenticate("user0", "pass0");
        // add role0 to user0, and user0 has one role:role0
        User user = new User("user0", "pass0");
        Role role = new Role("role0");
        Assert.assertTrue(AuthUtil.addRoleToUser(user, role));
        Assert.assertTrue(AuthUtil.getAllRoles(authToken).size() == 1);

        // add role1 to user0
        role = new Role("role1");
        Assert.assertTrue(AuthUtil.addRoleToUser(user, role));

        // user0 has two roles: role0 and role1
        Assert.assertTrue(AuthUtil.getAllRoles(authToken).size() == 2);

        // create error when user with wrong passWord
        user = new User("user1", "wrongPass");
        Assert.assertFalse(AuthUtil.addRoleToUser(user, role));

        // create error with does not exists role
        user = new User("user1", "pass1");
        role = new Role("role3");
        Assert.assertFalse(AuthUtil.addRoleToUser(user, role));
    }

    @Test
    public void testAuthenticate() {
        // the user3 is invalid, so we get error
        String authToken = AuthUtil.authenticate("user3", "pass3");
        Assert.assertTrue(Constant.ERROR.equals(authToken));

    }

    /**
     * test invalidate
     */
    @Test
    public void testInvalidate() {
        // authToken is wrong
        Assert.assertFalse(AuthUtil.invalidate("testAuthToken"));
        Assert.assertFalse(AuthUtil.invalidate("test0#pass0#123456"));

        // generate the authToken, and the authToken is valid
        String authToken = AuthUtil.authenticate("user0", "pass0");
        Assert.assertTrue(AuthUtil.invalidate(authToken));
    }

    /**
     * test check role
     */
    @Test
    public void testCheckRole() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // user0 has one role:role0
        String authToken = AuthUtil.authenticate("user0", "pass0");
        Role role0 = new Role("role0");
        Assert.assertTrue(Constant.TRUE.equals(AuthUtil.checkRole(authToken, role0)));

        Role role3 = new Role("role1");
        Assert.assertTrue(Constant.FALSE.equals(AuthUtil.checkRole(authToken, role3)));

        String errorAuthToken = "errorAuthToken";
        Assert.assertTrue(Constant.ERROR.equals(AuthUtil.checkRole(errorAuthToken, role0)));

        // this is the authToken of user(user0,pass0) at 2022-09-18 10:00:00, which is expired
        String expiredAuthToken = "user0#106438207#1663466400000";

        // set authToken set by reflection
        Set<String> authTokenSets = new HashSet<>();
        authTokenSets.add(expiredAuthToken);
        Class cls = Class.forName("com.huifeng.AuthUtil");
        Field field = cls.getDeclaredField("AUTHTOKEN_SET");
        field.setAccessible(true);
        field.set(null, authTokenSets);

        Assert.assertTrue(Constant.ERROR.equals(AuthUtil.checkRole(expiredAuthToken, role0)));

    }

    /**
     * test get all roles
     */
    @Test
    public void testGetAllRoles() {
        // the user0 has one role
        String authToken = AuthUtil.authenticate("user0", "pass0");
        Assert.assertTrue(AuthUtil.getAllRoles(authToken).size() == 1);

        // authToken is invalid
        String errorAuthToken = "errorAuthToken";
        Set<String> roleSets = AuthUtil.getAllRoles(errorAuthToken);
        Assert.assertTrue(roleSets.contains(Constant.ERROR));
    }
}