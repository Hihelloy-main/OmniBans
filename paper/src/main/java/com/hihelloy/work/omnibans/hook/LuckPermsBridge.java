package com.hihelloy.work.omnibans.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;

public final class LuckPermsBridge {

    private LuckPermsBridge() {
    }

    public static boolean hasPermission(UUID uuid, String node) throws Exception {
        LuckPerms luckPerms = LuckPermsProvider.get();
        UserManager userManager = luckPerms.getUserManager();
        User user = userManager.getUser(uuid);
        if (user == null) {
            user = userManager.loadUser(uuid).get();
        }
        if (user == null) {
            return false;
        }
        QueryOptions queryOptions = QueryOptions.defaultContextualOptions();
        return user.getCachedData().getPermissionData(queryOptions).checkPermission(node).asBoolean();
    }

}
