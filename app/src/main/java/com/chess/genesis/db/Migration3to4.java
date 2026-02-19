package com.chess.genesis.db;

import androidx.annotation.*;
import androidx.room.migration.*;
import androidx.sqlite.db.*;

public class Migration3to4 extends Migration
{
	public Migration3to4()
	{
		super(3, 4);
	}

	@Override
	public void migrate(@NonNull SupportSQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE " + ActiveGameEntity.TABLE_NAME + " ADD COLUMN hasArchiveData INTEGER NOT NULL DEFAULT 0");
	}
}
