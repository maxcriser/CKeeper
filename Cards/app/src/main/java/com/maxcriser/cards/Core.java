package com.maxcriser.cards;

import android.app.Application;
import android.content.Context;

import com.maxcriser.cards.database.DatabaseHelper;

public class Core extends Application {

    private DatabaseHelper mDatabaseHelper;

    public DatabaseHelper getDatabaseHelper(final Context pContext) {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = DatabaseHelper.Impl.newInstance(pContext);
        }
        return mDatabaseHelper;
    }

    public Core() {
        ContextHolder.set(this);
    }

}
