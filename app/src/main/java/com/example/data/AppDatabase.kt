package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TipEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tipDao(): TipDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eclassports_football.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial seed on database creation
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).tipDao().insertAll(getInitialSeedTips())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialSeedTips(): List<TipEntity> {
            return listOf(
                // ==================== FREE TIPS ====================
                // Date 1: 2026-10-12
                TipEntity(
                    category = TipCategory.FREE.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "16:00",
                    league = "English Premier League",
                    homeTeam = "Arsenal",
                    awayTeam = "Chelsea",
                    prediction = "Home Win & Over 1.5",
                    odds = 1.70,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.FREE.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "18:30",
                    league = "Spanish La Liga",
                    homeTeam = "Real Madrid",
                    awayTeam = "Sevilla",
                    prediction = "Home Win",
                    odds = 1.45,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.FREE.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "20:45",
                    league = "Italian Serie A",
                    homeTeam = "AC Milan",
                    awayTeam = "Napoli",
                    prediction = "Both Teams to Score",
                    odds = 1.75,
                    status = TipStatus.PENDING.name
                ),
                // Date 2: 2026-10-13
                TipEntity(
                    category = TipCategory.FREE.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "17:30",
                    league = "German Bundesliga",
                    homeTeam = "Bayern Munich",
                    awayTeam = "Borussia Dortmund",
                    prediction = "Over 3.5 Goals",
                    odds = 1.95,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.FREE.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "20:00",
                    league = "French Ligue 1",
                    homeTeam = "Paris Saint-Germain",
                    awayTeam = "Marseille",
                    prediction = "Home Win & Over 2.5",
                    odds = 1.62,
                    status = TipStatus.PENDING.name
                ),

                // ==================== 2+ TIPS VIP (Active) ====================
                // Date 1: 2026-10-12
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "15:00",
                    league = "English Premier League",
                    homeTeam = "Manchester City",
                    awayTeam = "Everton",
                    prediction = "Home Win",
                    odds = 1.38,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "17:00",
                    league = "Italian Serie A",
                    homeTeam = "Inter Milan",
                    awayTeam = "Torino",
                    prediction = "Home Win & Over 1.5",
                    odds = 1.52,
                    status = TipStatus.PENDING.name
                ),
                // Date 2: 2026-10-13
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "18:00",
                    league = "Spanish La Liga",
                    homeTeam = "Barcelona",
                    awayTeam = "Valencia",
                    prediction = "Home Win & Over 2.5",
                    odds = 1.65,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "20:30",
                    league = "Dutch Eredivisie",
                    homeTeam = "Ajax",
                    awayTeam = "Feyenoord",
                    prediction = "Over 2.5 Goals",
                    odds = 1.50,
                    status = TipStatus.PENDING.name
                ),

                // ==================== 3+ TIPS VIP (Active) ====================
                // Date 1: 2026-10-12
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "14:30",
                    league = "German Bundesliga",
                    homeTeam = "Bayer Leverkusen",
                    awayTeam = "RB Leipzig",
                    prediction = "Over 2.5 Goals",
                    odds = 1.60,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "16:30",
                    league = "English Premier League",
                    homeTeam = "Liverpool",
                    awayTeam = "Aston Villa",
                    prediction = "Home Win",
                    odds = 1.48,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "19:00",
                    league = "Spanish La Liga",
                    homeTeam = "Atletico Madrid",
                    awayTeam = "Real Sociedad",
                    prediction = "Home Win",
                    odds = 1.42,
                    status = TipStatus.PENDING.name
                ),
                // Date 2: 2026-10-13
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "19:45",
                    league = "Portuguese Primeira Liga",
                    homeTeam = "Benfica",
                    awayTeam = "FC Porto",
                    prediction = "Over 2.5 Goals",
                    odds = 1.82,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-13",
                    kickoffTime = "21:00",
                    league = "Italian Serie A",
                    homeTeam = "Juventus",
                    awayTeam = "Fiorentina",
                    prediction = "Home Win",
                    odds = 1.75,
                    status = TipStatus.PENDING.name
                ),

                // ==================== 5+ TIPS VIP (Active) ====================
                // Date 1: 2026-10-12
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "13:30",
                    league = "English Championship",
                    homeTeam = "Leeds United",
                    awayTeam = "Leicester City",
                    prediction = "Both Teams to Score",
                    odds = 1.72,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "15:30",
                    league = "German Bundesliga",
                    homeTeam = "VfB Stuttgart",
                    awayTeam = "Wolfsburg",
                    prediction = "Home Win",
                    odds = 1.68,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "18:00",
                    league = "Italian Serie A",
                    homeTeam = "AS Roma",
                    awayTeam = "Lazio",
                    prediction = "Over 2.5 Goals",
                    odds = 1.85,
                    status = TipStatus.PENDING.name
                ),

                // ==================== SPECIAL VIP (Active) ====================
                // Date 1: 2026-10-12
                TipEntity(
                    category = TipCategory.VIP_SPECIAL.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "17:00",
                    league = "Turkish Super Lig",
                    homeTeam = "Galatasaray",
                    awayTeam = "Besiktas",
                    prediction = "Home Win & Over 2.5",
                    odds = 2.10,
                    status = TipStatus.PENDING.name
                ),
                TipEntity(
                    category = TipCategory.VIP_SPECIAL.id,
                    matchDate = "2026-10-12",
                    kickoffTime = "19:45",
                    league = "Scottish Premiership",
                    homeTeam = "Celtic",
                    awayTeam = "Rangers",
                    prediction = "Both Teams to Score & Over 2.5",
                    odds = 2.05,
                    status = TipStatus.PENDING.name
                ),

                // ==================== SETTLED HISTORY TIPS ====================
                // Category 1: 2+ Tips VIP History
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "15:00",
                    league = "English Premier League",
                    homeTeam = "Newcastle United",
                    awayTeam = "Wolves",
                    prediction = "Home Win",
                    odds = 1.55,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "17:30",
                    league = "Italian Serie A",
                    homeTeam = "Atalanta",
                    awayTeam = "Genoa",
                    prediction = "Over 1.5 Goals",
                    odds = 1.35,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-11",
                    kickoffTime = "14:00",
                    league = "Spanish La Liga",
                    homeTeam = "Villarreal",
                    awayTeam = "Celta Vigo",
                    prediction = "Home Win",
                    odds = 1.62,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_2_PLUS.id,
                    matchDate = "2026-10-11",
                    kickoffTime = "18:30",
                    league = "German Bundesliga",
                    homeTeam = "Eintracht Frankfurt",
                    awayTeam = "Augsburg",
                    prediction = "Both Teams to Score",
                    odds = 1.50,
                    status = TipStatus.LOST.name
                ),

                // Category 2: 3+ Tips VIP History
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "16:00",
                    league = "English Premier League",
                    homeTeam = "Tottenham Hotspur",
                    awayTeam = "West Ham United",
                    prediction = "Home Win & Over 1.5",
                    odds = 1.72,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "19:00",
                    league = "French Ligue 1",
                    homeTeam = "Monaco",
                    awayTeam = "Lille",
                    prediction = "Both Teams to Score",
                    odds = 1.65,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_3_PLUS.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "21:00",
                    league = "Spanish La Liga",
                    homeTeam = "Sevilla",
                    awayTeam = "Mallorca",
                    prediction = "Under 2.5 Goals",
                    odds = 1.48,
                    status = TipStatus.WON.name
                ),

                // Category 3: 5+ Tips VIP History
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-09",
                    kickoffTime = "18:00",
                    league = "Dutch Eredivisie",
                    homeTeam = "PSV Eindhoven",
                    awayTeam = "AZ Alkmaar",
                    prediction = "Over 3.5 Goals",
                    odds = 2.20,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-09",
                    kickoffTime = "20:30",
                    league = "English Championship",
                    homeTeam = "Burnley",
                    awayTeam = "Sheffield United",
                    prediction = "Home Win",
                    odds = 1.85,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_5_PLUS.id,
                    matchDate = "2026-10-09",
                    kickoffTime = "21:45",
                    league = "Portuguese Primeira Liga",
                    homeTeam = "SC Braga",
                    awayTeam = "Vitoria Guimaraes",
                    prediction = "Over 2.5 Goals",
                    odds = 1.70,
                    status = TipStatus.WON.name
                ),

                // Category 4: Special VIP History
                TipEntity(
                    category = TipCategory.VIP_SPECIAL.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "17:00",
                    league = "German Bundesliga",
                    homeTeam = "TSG Hoffenheim",
                    awayTeam = "Mainz 05",
                    prediction = "Both Teams to Score & Over 2.5",
                    odds = 2.15,
                    status = TipStatus.WON.name
                ),
                TipEntity(
                    category = TipCategory.VIP_SPECIAL.id,
                    matchDate = "2026-10-10",
                    kickoffTime = "19:30",
                    league = "Italian Serie A",
                    homeTeam = "Lazio",
                    awayTeam = "Bologna",
                    prediction = "Home Win",
                    odds = 2.05,
                    status = TipStatus.LOST.name
                )
            )
        }
    }
}
