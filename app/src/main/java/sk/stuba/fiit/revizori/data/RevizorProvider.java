/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.stuba.fiit.revizori.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RevizorProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RevizorDbHelper mOpenHelper;


    //   content://authority/revizor/
    static final int REVIZOR = 100;

    //   content://authority/revizor/id
    static final int REVIZOR_WITH_ID = 101;

    private static final SQLiteQueryBuilder queryBuilder;

    // static initialization block
    // this bloc is automatically executed during constructor, but only during first call!
    // unlike { ... }, which is executed every time a new instance is created
    static {
        queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(RevizorContract.RevizorEntry.TABLE_NAME);
    }


    private Cursor getRevizorById(Uri uri, String[] projection, String sortOrder) {
        String idFromUri = RevizorContract.RevizorEntry.getRevizorIdFromUri(uri);

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                RevizorContract.RevizorEntry.COLUMN_OBJECT_ID + "=" + idFromUri,
                null, // selectionArgs, wtf is this?
                null,
                null,
                sortOrder
        );
    }


    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RevizorContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RevizorContract.PATH_REVIZOR, REVIZOR);
        matcher.addURI(authority, RevizorContract.PATH_REVIZOR + "/*", REVIZOR_WITH_ID);

        return matcher;
    }


    /*
        Students: We've coded this for you.  We just create a new RevizorDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new RevizorDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REVIZOR:
                return RevizorContract.RevizorEntry.CONTENT_TYPE;
            case REVIZOR_WITH_ID:
                return RevizorContract.RevizorEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "revizor/*"
            case REVIZOR_WITH_ID: {
                retCursor = getRevizorById(uri, projection, sortOrder);
                break;
            }
            // "revizor"
            case REVIZOR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RevizorContract.RevizorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case REVIZOR: {
                long _id = db.insert(RevizorContract.RevizorEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RevizorContract.RevizorEntry.buildRevizorUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

//        // this makes delete all rows return the number of rows deleted
//        if ( null == selection ) selection = "1";

        switch (match) {
            case REVIZOR:
                rowsDeleted = db.delete(
                        RevizorContract.RevizorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIZOR_WITH_ID:
                rowsDeleted = db.delete(
                        RevizorContract.RevizorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case REVIZOR:
                rowsUpdated = db.update(RevizorContract.RevizorEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}