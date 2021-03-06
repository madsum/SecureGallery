//------------------------------------------------------------------------------
// Copyright (c) 2012 Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------

package fi.masum.securegallery;

import org.json.JSONObject;

public class SkyDriveAlbum extends SkyDriveObject {

    public static final String TYPE = "album";

    public SkyDriveAlbum(JSONObject object) {
        super(object);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getCount() {
        return mObject.optInt("count");
    }
}
