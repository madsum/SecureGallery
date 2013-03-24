//------------------------------------------------------------------------------
// Copyright (c) 2012 Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------

package fi.masum.securegallery;

final public class Config {
    public static final String CLIENT_ID = "000000004C0E227F";

    public static final String[] SCOPES = {
        "wl.signin",
        "wl.basic",
        "wl.offline_access",
        "wl.skydrive_update",
        "wl.contacts_create",
    };

    private Config() {
        throw new AssertionError("Unable to create Config object.");
    }
}
