package com.megazegan.vaultwatch.face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TerminalActivity extends Activity {
    private PipBoyTerminalView terminalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setStatusBarColor(Color.BLACK);
        terminalView = new PipBoyTerminalView(this, viewFromIntent(getIntent()));
        setContentView(terminalView);
        launchExternalSection(viewFromIntent(getIntent()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (terminalView != null) {
            terminalView.randomizeSession();
            int next = viewFromIntent(intent);
            terminalView.setSection(next);
            launchExternalSection(next);
        }
    }

    private int viewFromIntent(Intent intent) {
        Uri data = intent == null ? null : intent.getData();
        String value = data == null ? "" : data.getPath();
        if (value == null) {
            value = "";
        }
        if (value.contains("inventory")) return 1;
        if (value.contains("data")) return 2;
        if (value.contains("map")) return 3;
        if (value.contains("radio")) return 4;
        return 0;
    }

        private void launchExternalSection(int section) {
        return;
    }

    private boolean launchExternalPackage(String packageName) {
        Intent intent;
        if ("com.google.android.apps.maps".equals(packageName)) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(packageName, "com.google.android.apps.gmmwearable.MainActivity");
        } else if ("com.spotify.music".equals(packageName)) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(packageName, "com.spotify.wear.main.MainActivity");
        } else {
            intent = getPackageManager().getLaunchIntentForPackage(packageName);
        }
        if (intent == null) {
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }

    private static final class PipBoyTerminalView extends View {
        private static final String[] TABS = {"STAT", "INV", "DATA", "MAP", "RAD", "COM", "SYS", "SEC", "SURV", "WX"};
        private static final String[] THEMES = {"GREEN", "AMBER", "BLUE"};
        private static final String[] ITEM_NAMES = {
                "SPOTIFY RELAY", "TIMER FUSE", "ALARM BEACON", "FLASH MODULE",
                "CALC HOLOTAPE", "BT TRANSCEIVER", "WIFI KEY", "STIM KIT",
                "RADAWAY", "VAULT 111 SUIT", "NUKA GRENADE", "LASER MUSKET",
                "ROAD LEATHERS", "PIPE REVOLVER", "BOTTLECAP MINE", "JET INHALER",
                "COMBAT ARMOR", "PLASMA CELL", "SUGAR BOMBS", "DOGMEAT BANDANA",
                "FUSION CORE", "MENTATS", "GROGNAK AXE", "NOTE TAPE"
        };
        private static final String[] ITEM_TYPES = {"WEAPON", "AID", "TOOL", "TECH", "JUNK"};
        private static final String[] QUEST_NAMES = {
                "OPEN SIGNAL", "SUPPLY RUN", "VAULT CACHE", "RELAY ECHO",
                "OLD WORLD BLUES", "WATER CHIP TRACE", "NICK'S LEAD",
                "ATOM'S GLOW", "CARAVAN CALL", "RED ROCKET CACHE",
                "SILVER SHROUD", "LOST PATROL", "GNN DISTRESS", "WALK 4K STEPS",
                "DRINK WATER", "CHECK NOTES", "SECURITY SWEEP", "GYM ROUTE"
        };
        private static final String[] QUEST_NOTES = {
                "FOLLOW CARRIER TONE NORTH-EAST",
                "THREE WATER CELLS REMAIN",
                "CACHE ACCEPTS SIX PULSE HANDSHAKE",
                "CHECK ROOFTOP ANTENNA AFTER DUSK",
                "MARKER MOVED BELOW THE OVERPASS",
                "LOCAL FERALS REACT TO RADIO BURSTS",
                "RETURN BEFORE RAD STORM ARRIVAL",
                "SETTLEMENT REQUEST FLAGGED URGENT"
        };
        private static final String[] QUEST_STATUS = {"ACTIVE", "47%", "NEW", "HOLD", "TRACE", "SYNC", "DONE"};
        private static final String[] EFFECTS = {
                "WELL RESTED", "FOCUSED", "CAFFEINATED", "ENERGIZED",
                "OVER-ENCUMBERED", "LOW POWER MODE", "DEHYDRATED", "SLEEP DEPRIVED"
        };
        private static final String[] COMMS = {
                "SMS PREVIEW", "MISSED CALL", "DISCORD ALERT", "VAULT MAIL"
        };
        private static final String[] SYSTEM_ROWS = {
                "BATTERY", "RAM USAGE", "CORE TEMP", "STORAGE"
        };
        private static final String[] SECURITY_ROWS = {
                "PASS GEN", "QR SCAN", "NFC READER", "PING TEST"
        };
        private static final String[] SURVIVAL_ROWS = {
                "WATER", "MEDS", "STEP GOAL", "WORKOUT"
        };
        private static final String[] WEATHER_ROWS = {
                "TEMP", "RAIN", "UV INDEX", "WIND"
        };
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect mapSrc = new Rect();
        private final RectF rect = new RectF();
        private final Path circle = new Path();
        private final Random random = new Random();
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/dd", Locale.US);
        private final Bitmap vaultBoy;
        private final Bitmap helmet;
        private final Bitmap bolt;
        private final Bitmap rad;
        private final Bitmap gun;
        private final Bitmap pipMap;
        private Typeface font = Typeface.MONOSPACE;
        private int section;
        private int selectedInventory;
        private int selectedData;
        private int selectedStation;
        private int selectedAux;
        private int theme;
        private int hp;
        private int ap;
        private int radiation;
        private int head;
        private int arm;
        private int leg;
        private int core;
        private int heartRate;
        private int stress;
        private int hydration;
        private int sleepScore;
        private int steps;
        private int oxygen;
        private int tempSim;
        private int carryWeight;
        private int radioSignal;
        private int speed;
        private int altitude;
        private int ramUse;
        private int cpuTemp;
        private int storageUse;
        private int rainChance;
        private int windSpeed;
        private int uvIndex;
        private int nextWater;
        private long lastStatTick;
        private String[] inventoryItems = new String[4];
        private String[] inventoryTypes = new String[4];
        private Bitmap[] inventoryIcons = new Bitmap[4];
        private int[] inventoryDamage = new int[4];
        private int[] inventoryCount = new int[4];
        private int[] inventoryValue = new int[4];
        private int[] inventoryWeight = new int[4];
        private String[] dataLabels = new String[3];
        private String[] dataValues = new String[3];
        private String[] dataNotes = new String[3];
        private int[] special = new int[7];
        private String activeEffect = "WELL RESTED";
        private String scannerSignal = "UNKNOWN SIGNAL";
        private String weatherStatus = "CLEAR";
        private String securityToken = "VX-111-TEC";
        private int text = Color.rgb(119, 255, 114);
        private int dim = Color.argb(150, 119, 255, 114);
        private int faint = Color.argb(42, 119, 255, 114);

        PipBoyTerminalView(Context context, int startSection) {
            super(context);
            section = startSection;
            setFocusable(true);
            try {
                font = Typeface.createFromAsset(context.getAssets(), "monofonto.ttf");
            } catch (RuntimeException ignored) {
                font = Typeface.MONOSPACE;
            }
            vaultBoy = loadBitmap(context, "img/imported-pipboy/VaultBoy.png");
            helmet = loadBitmap(context, "img/ico/helmet.png");
            bolt = loadBitmap(context, "img/ico/bolt.png");
            rad = loadBitmap(context, "img/ico/radioactive.png");
            gun = loadBitmap(context, "img/ico/gun.png");
            pipMap = loadBitmap(context, "img/map.webp");
            randomizeSession();
            postInvalidateDelayed(1000);
        }

        void setSection(int next) {
            section = Math.max(0, Math.min(TABS.length - 1, next));
            selectedAux = 0;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int save = canvas.save();
            float size = Math.min(getWidth(), getHeight());
            float scale = size / 450f;
            canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
            canvas.scale(scale, scale);
            circle.reset();
            circle.addCircle(225f, 225f, 224f, Path.Direction.CW);
            canvas.clipPath(circle);

            drawFrame(canvas);
            updateStats();
            drawHeader(canvas);
            drawTabs(canvas);
            switch (section) {
                case 1:
                    drawInventory(canvas);
                    break;
                case 2:
                    drawData(canvas);
                    break;
                case 3:
                    drawMap(canvas);
                    break;
                case 4:
                    drawRadio(canvas);
                    break;
                case 5:
                    drawComms(canvas);
                    break;
                case 6:
                    drawSystem(canvas);
                    break;
                case 7:
                    drawSecurity(canvas);
                    break;
                case 8:
                    drawSurvival(canvas);
                    break;
                case 9:
                    drawWeather(canvas);
                    break;
                default:
                    drawStatus(canvas);
                    break;
            }
            drawFooter(canvas);
            drawScanlines(canvas);
            canvas.restoreToCount(save);
            postInvalidateDelayed(1000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }
            float size = Math.min(getWidth(), getHeight());
            float x = (event.getX() - (getWidth() - size) / 2f) * 450f / size;
            float y = (event.getY() - (getHeight() - size) / 2f) * 450f / size;
            if (y >= 128 && y <= 166) {
                int visibleStart = visibleTabStart();
                int next = visibleStart + Math.max(0, Math.min(4, (int) ((x - 58) / 74f)));
                next = Math.max(0, Math.min(TABS.length - 1, next));
                goToSection(next);
                return true;
            }
            if (section == 1 && x >= 68 && x <= 228 && y >= 175 && y <= 343) {
                selectedInventory = Math.max(0, Math.min(3, (int) ((y - 175) / 42f)));
                invalidate();
                return true;
            }
            if (section == 2 && x >= 72 && x <= 378 && y >= 178 && y <= 300) {
                selectedData = Math.max(0, Math.min(2, (int) ((y - 178) / 44f)));
                invalidate();
                return true;
            }
            if (section == 3 && x >= 70 && x <= 380 && y >= 174 && y <= 372) {
                launchPackage("com.google.android.apps.maps");
                return true;
            }
            if (section == 4 && x >= 88 && x <= 378 && y >= 262 && y <= 376) {
                selectedStation = Math.max(0, Math.min(2, (int) ((y - 262) / 40f)));
                invalidate();
                launchPackage("com.spotify.music");
                return true;
            }
            if (section >= 5 && x >= 72 && x <= 378 && y >= 176 && y <= 340) {
                selectedAux = Math.max(0, Math.min(3, (int) ((y - 176) / 40f)));
                randomizeAuxiliarySignal();
                invalidate();
                return true;
            }
            if (y >= 374 && y <= 425) {
                if (x < 156) stepWithinSection(-1);
                else if (x > 294) stepWithinSection(1);
                else cycleTheme();
                return true;
            }
            return true;
        }

        private void drawFrame(Canvas canvas) {
            canvas.drawColor(Color.rgb(2, 6, 3));
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(5, 16, 8));
            canvas.drawCircle(225, 225, 222, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(90, 119, 255, 114));
            rect.set(18, 18, 432, 432);
            canvas.drawArc(rect, 207, 126, false, paint);
        }

        private void drawHeader(Canvas canvas) {
            text(canvas, "PIP-BOY 3000", 225, 36, 14, dim, Paint.Align.CENTER);
            text(canvas, timeFormat.format(new Date()), 225, 90, 56, text, Paint.Align.CENTER);
            text(canvas, dateFormat.format(new Date()).toUpperCase(Locale.US), 225, 112, 14, dim, Paint.Align.CENTER);
        }

        private void drawTabs(Canvas canvas) {
            paint.setStrokeWidth(1.5f);
            int visibleStart = visibleTabStart();
            for (int i = 0; i < 5; i++) {
                int tabIndex = visibleStart + i;
                float left = 58 + i * 74f;
                rect.set(left, 129, left + 70, 163);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(tabIndex == section ? faint : Color.TRANSPARENT);
                canvas.drawRect(rect, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(tabIndex == section ? text : dim);
                canvas.drawRect(rect, paint);
                text(canvas, TABS[tabIndex], left + 35, 152, 16, tabIndex == section ? text : dim, Paint.Align.CENTER);
            }
            if (visibleStart > 0) text(canvas, "<", 42, 152, 18, dim, Paint.Align.CENTER);
            if (visibleStart + 5 < TABS.length) text(canvas, ">", 408, 152, 18, dim, Paint.Align.CENTER);
        }

        private int visibleTabStart() {
            return Math.min(Math.max(0, section - 2), TABS.length - 5);
        }

        private void drawStatus(Canvas canvas) {
            rect.set(68, 171, 190, 310);
            box(canvas, rect, false);
            long phase = System.currentTimeMillis() % 1400L;
            float wave = (float) Math.sin((phase / 1400f) * Math.PI * 2f);
            float bob = wave * 3f;
            float pulse = 1f + wave * 0.035f;
            drawBitmap(canvas, vaultBoy, 91 - (76 * (pulse - 1f) / 2f), 186 + bob, 76 * pulse, 92 * pulse, text);
            text(canvas, activeEffect, 129, 300, 12, dim, Paint.Align.CENTER);
            meter(canvas, helmet, "HP", 205, 188, hp, text);
            meter(canvas, bolt, "AP", 205, 239, ap, text);
            meter(canvas, rad, "RAD", 205, 290, radiation, Color.rgb(255, 107, 74));
            statBox(canvas, "HR " + heartRate, 68, 318, 74);
            statBox(canvas, "O2 " + oxygen, 146, 318, 74);
            statBox(canvas, "H2O " + hydration, 224, 318, 74);
            statBox(canvas, "SLP " + sleepScore, 302, 318, 80);
            text(canvas, "S " + special[0] + "  P " + special[1] + "  E " + special[2] + "  C " + special[3], 225, 363, 14, dim, Paint.Align.CENTER);
            text(canvas, "I " + special[4] + "  A " + special[5] + "  L " + special[6] + "  FAT " + (100 - ap), 225, 379, 14, dim, Paint.Align.CENTER);
        }

        private void drawInventory(Canvas canvas) {
            for (int i = 0; i < inventoryItems.length; i++) {
                rect.set(68, 175 + i * 42, 228, 209 + i * 42);
                boolean active = i == selectedInventory;
                box(canvas, rect, active);
                drawBitmap(canvas, inventoryIcons[i], 76, 180 + i * 42, 22, 22, text);
                text(canvas, inventoryItems[i], 105, 198 + i * 42, 14, active ? text : dim, Paint.Align.LEFT);
            }
            rect.set(244, 175, 382, 342);
            box(canvas, rect, false);
            drawBitmap(canvas, inventoryIcons[selectedInventory], 290, 189, 48, 48, text);
            text(canvas, inventoryItems[selectedInventory], 313, 258, 17, text, Paint.Align.CENTER);
            text(canvas, inventoryTypes[selectedInventory], 313, 276, 14, dim, Paint.Align.CENTER);
            text(canvas, "DMG " + String.format(Locale.US, "%03d", inventoryDamage[selectedInventory]), 268, 298, 15, dim, Paint.Align.LEFT);
            text(canvas, "WT " + inventoryWeight[selectedInventory] + "  VAL " + String.format(Locale.US, "%03d", inventoryValue[selectedInventory]), 268, 322, 15, dim, Paint.Align.LEFT);
            text(canvas, "LOAD " + carryWeight + "/220", 313, 358, 14, carryWeight > 180 ? Color.rgb(255, 107, 74) : dim, Paint.Align.CENTER);
        }

        private void drawData(Canvas canvas) {
            for (int i = 0; i < dataLabels.length; i++) {
                row(canvas, 72, 178 + i * 44, dataLabels[i], dataValues[i], i == selectedData);
            }
            rect.set(72, 316, 378, 360);
            box(canvas, rect, false);
            text(canvas, dataNotes[selectedData], 225, 342, 14, dim, Paint.Align.CENTER);
        }

        private void drawMap(Canvas canvas) {
            rect.set(70, 174, 380, 335);
            box(canvas, rect, false);
            if (pipMap != null) {
                int sourceWidth = pipMap.getWidth();
                int sourceHeight = pipMap.getHeight();
                int cropHeight = Math.min(sourceHeight, Math.round(sourceWidth * rect.height() / rect.width()));
                int cropTop = Math.max(0, (sourceHeight - cropHeight) / 2);
                mapSrc.set(0, cropTop, sourceWidth, cropTop + cropHeight);
                paint.setFilterBitmap(true);
                paint.setDither(true);
                paint.setAlpha(255);
                paint.setColorFilter(null);
                canvas.drawBitmap(pipMap, mapSrc, rect, paint);
                paint.setAlpha(255);
                paint.setColorFilter(null);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(28, 0, 20, 5));
                canvas.drawRect(rect, paint);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1f);
                paint.setColor(dim);
                for (int i = 0; i < 9; i++) {
                    canvas.drawLine(80 + i * 34, 178, 80 + i * 34, 332, paint);
                    canvas.drawLine(72, 182 + i * 18, 378, 182 + i * 18, paint);
                }
                paint.setStrokeWidth(4f);
                paint.setColor(text);
                Path road = new Path();
                road.moveTo(88, 292);
                road.cubicTo(130, 248, 171, 270, 208, 230);
                road.cubicTo(242, 192, 300, 204, 360, 188);
                canvas.drawPath(road, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(text);
            canvas.drawRect(rect, paint);
            pin(canvas, 294, 212, "R34");
            pin(canvas, 154, 282, "TUN");
            text(canvas, "GPS 41.008N 28.978E", 225, 316, 12, dim, Paint.Align.CENTER);
            text(canvas, "SPD " + speed + "KMH  ALT " + altitude + "M", 225, 331, 12, dim, Paint.Align.CENTER);
            rect.set(88, 342, 362, 372);
            box(canvas, rect, true);
            text(canvas, "OPEN GOOGLE MAPS", 225, 363, 15, text, Paint.Align.CENTER);
        }

        private void drawRadio(Canvas canvas) {
            String[] stations = {"WVR 88.1", "RANGER NET", "CLASSICAL"};
            String[] states = {"PLAY", "SYNC", "OPEN"};
            drawBitmap(canvas, rad, 186, 176, 78, 78, text);
            meterBar(canvas, "SIGNAL", 88, 252, radioSignal, text);
            for (int i = 0; i < stations.length; i++) {
                row(canvas, 88, 276 + i * 34, stations[i], states[i], i == selectedStation);
            }
            text(canvas, scannerSignal, 225, 374, 13, dim, Paint.Align.CENTER);
        }

        private void drawComms(Canvas canvas) {
            String[] values = {"1 NEW", "NONE", "SYNC", "LOCKED"};
            drawMenuRows(canvas, COMMS, values);
            detailPanel(canvas, "COMMS RELAY", "NOTIFICATIONS ARE READ-ONLY", "NO BACKGROUND SPAM");
        }

        private void drawSystem(Canvas canvas) {
            String[] values = {
                    "83%",
                    ramUse + "%",
                    cpuTemp + "C",
                    storageUse + "%"
            };
            drawMenuRows(canvas, SYSTEM_ROWS, values);
            detailPanel(canvas, "POWER MODE", "LOW REFRESH / AMOLED SAFE", "APK v2.6");
        }

        private void drawSecurity(Canvas canvas) {
            String[] values = {"READY", "IDLE", "WAIT", "12MS"};
            drawMenuRows(canvas, SECURITY_ROWS, values);
            detailPanel(canvas, "SECURITY", securityToken, "LOCAL TO WATCH ONLY");
        }

        private void drawSurvival(Canvas canvas) {
            String[] values = {
                    hydration + "%",
                    "22:00",
                    steps + "/6K",
                    ap > 65 ? "READY" : "REST"
            };
            drawMenuRows(canvas, SURVIVAL_ROWS, values);
            detailPanel(canvas, "SURVIVAL", activeEffect, "NEXT WATER IN " + nextWater + "M");
        }

        private void drawWeather(Canvas canvas) {
            String[] values = {
                    tempSim + "C",
                    rainChance + "%",
                    String.valueOf(uvIndex),
                    windSpeed + "KMH"
            };
            drawMenuRows(canvas, WEATHER_ROWS, values);
            detailPanel(canvas, "WEATHER", weatherStatus, "RAD STORM: " + (radiation > 28 ? "WATCH" : "CLEAR"));
        }

        private void drawMenuRows(Canvas canvas, String[] labels, String[] values) {
            for (int i = 0; i < labels.length; i++) {
                row(canvas, 72, 176 + i * 40, labels[i], values[i], i == selectedAux);
            }
        }

        private void detailPanel(Canvas canvas, String title, String lineOne, String lineTwo) {
            rect.set(72, 342, 378, 374);
            box(canvas, rect, true);
            text(canvas, title, 88, 363, 14, text, Paint.Align.LEFT);
            text(canvas, lineOne, 362, 363, 12, text, Paint.Align.RIGHT);
            text(canvas, lineTwo, 225, 336, 12, dim, Paint.Align.CENTER);
        }

        private void drawFooter(Canvas canvas) {
            rect.set(110, 382, 146, 416);
            box(canvas, rect, false);
            text(canvas, "<", 128, 407, 27, text, Paint.Align.CENTER);
            rect.set(156, 382, 222, 416);
            box(canvas, rect, true);
            text(canvas, THEMES[theme], 189, 404, 14, text, Paint.Align.CENTER);
            text(canvas, "BAT 83", 236, 397, 14, dim, Paint.Align.LEFT);
            text(canvas, "SIG 74", 236, 413, 14, dim, Paint.Align.LEFT);
            rect.set(304, 382, 340, 416);
            box(canvas, rect, false);
            text(canvas, ">", 322, 407, 27, text, Paint.Align.CENTER);
        }

        private boolean launchPackage(String packageName) {
            Context context = getContext();
            if (context instanceof TerminalActivity) {
                return ((TerminalActivity) context).launchExternalPackage(packageName);
            }
            return false;
        }

        private void goToSection(int next) {
            setSection(next);
        }

        private void stepWithinSection(int direction) {
            if (section == 1) {
                selectedInventory = (selectedInventory + direction + 4) % 4;
                invalidate();
            } else if (section == 2) {
                selectedData = (selectedData + direction + 3) % 3;
                invalidate();
            } else if (section >= 5) {
                selectedAux = (selectedAux + direction + 4) % 4;
                randomizeAuxiliarySignal();
                invalidate();
            } else {
                goToSection(section + direction < 0 ? TABS.length - 1 : (section + direction) % TABS.length);
            }
        }

        private void randomizeSession() {
            hp = 55 + random.nextInt(44);
            ap = 45 + random.nextInt(50);
            radiation = 8 + random.nextInt(28);
            head = 65 + random.nextInt(34);
            arm = 55 + random.nextInt(44);
            leg = 50 + random.nextInt(45);
            core = 60 + random.nextInt(38);
            heartRate = 64 + random.nextInt(42);
            stress = 8 + random.nextInt(62);
            hydration = 45 + random.nextInt(54);
            sleepScore = 52 + random.nextInt(47);
            steps = 800 + random.nextInt(5200);
            oxygen = 94 + random.nextInt(6);
            tempSim = 12 + random.nextInt(22);
            ramUse = 28 + random.nextInt(48);
            cpuTemp = 29 + random.nextInt(16);
            storageUse = 34 + random.nextInt(46);
            rainChance = random.nextInt(90);
            windSpeed = 4 + random.nextInt(28);
            uvIndex = 1 + random.nextInt(8);
            nextWater = 20 + random.nextInt(40);
            speed = random.nextInt(8);
            altitude = 12 + random.nextInt(180);
            radioSignal = 35 + random.nextInt(62);
            activeEffect = EFFECTS[random.nextInt(EFFECTS.length)];
            scannerSignal = randomSignal();
            weatherStatus = rainChance > 55 ? "RAIN FRONT" : radiation > 28 ? "RAD HAZE" : "CLEAR SKIES";
            securityToken = "VX-" + (100 + random.nextInt(899)) + "-TEC";
            for (int i = 0; i < special.length; i++) {
                special[i] = 3 + random.nextInt(8);
            }
            selectedInventory = 0;
            selectedData = 0;
            selectedStation = 0;
            selectedAux = 0;
            carryWeight = 0;
            Bitmap[] icons = {gun, bolt, helmet, rad};
            boolean[] usedItems = new boolean[ITEM_NAMES.length];
            for (int i = 0; i < inventoryItems.length; i++) {
                int index;
                do {
                    index = random.nextInt(ITEM_NAMES.length);
                } while (usedItems[index]);
                usedItems[index] = true;
                inventoryItems[i] = ITEM_NAMES[index];
                inventoryTypes[i] = ITEM_TYPES[random.nextInt(ITEM_TYPES.length)];
                inventoryIcons[i] = icons[random.nextInt(icons.length)];
                inventoryDamage[i] = random.nextBoolean() ? random.nextInt(46) : 0;
                inventoryCount[i] = 1 + random.nextInt(99);
                inventoryValue[i] = 12 + random.nextInt(220);
                inventoryWeight[i] = 3 + random.nextInt(58);
                carryWeight += inventoryWeight[i];
            }
            if (carryWeight > 180) {
                activeEffect = "OVER-ENCUMBERED";
            } else if (hydration < 45) {
                activeEffect = "DEHYDRATED";
            }
            boolean[] usedQuests = new boolean[QUEST_NAMES.length];
            for (int i = 0; i < dataLabels.length; i++) {
                int index;
                do {
                    index = random.nextInt(QUEST_NAMES.length);
                } while (usedQuests[index]);
                usedQuests[index] = true;
                dataLabels[i] = QUEST_NAMES[index];
                dataValues[i] = QUEST_STATUS[random.nextInt(QUEST_STATUS.length)];
                dataNotes[i] = QUEST_NOTES[random.nextInt(QUEST_NOTES.length)];
            }
        }

        private void updateStats() {
            long now = System.currentTimeMillis();
            if (now - lastStatTick < 900L) {
                return;
            }
            lastStatTick = now;
            hp = drift(hp, 42, 99, 5);
            ap = drift(ap, 35, 99, 6);
            radiation = drift(radiation, 2, 38, 4);
            head = drift(head, 55, 99, 3);
            arm = drift(arm, 45, 99, 3);
            leg = drift(leg, 40, 99, 3);
            core = drift(core, 50, 99, 3);
            heartRate = drift(heartRate, 58, 112, 3);
            stress = drift(stress, 4, 92, 3);
            hydration = drift(hydration, 30, 99, 2);
            oxygen = drift(oxygen, 92, 100, 1);
            radioSignal = drift(radioSignal, 20, 99, 4);
            speed = drift(speed, 0, 9, 1);
        }

        private int drift(int value, int min, int max, int amount) {
            int next = value + random.nextInt(amount * 2 + 1) - amount;
            if (next < min) return min;
            if (next > max) return max;
            return next;
        }

        private void randomizeAuxiliarySignal() {
            scannerSignal = randomSignal();
            radioSignal = drift(radioSignal, 20, 99, 12);
            ramUse = drift(ramUse, 18, 88, 8);
            cpuTemp = drift(cpuTemp, 26, 49, 3);
        }

        private String randomSignal() {
            String[] signals = {
                    "UNKNOWN SIGNAL", "WEAK TRANSMISSION", "ENCRYPTED DEVICE",
                    "VAULT PING", "STATIC BURST", "LOCAL BEACON"
            };
            return signals[random.nextInt(signals.length)];
        }

        private void meter(Canvas canvas, Bitmap icon, String label, float x, float y, int value, int color) {
            drawBitmap(canvas, icon, x, y - 13, 21, 21, color);
            text(canvas, label, x + 31, y + 3, 16, color, Paint.Align.LEFT);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(color);
            rect.set(x + 74, y - 8, x + 205, y + 4);
            canvas.drawRect(rect, paint);
            paint.setStyle(Paint.Style.FILL);
            rect.set(x + 74, y - 8, x + 74 + 131 * value / 100f, y + 4);
            canvas.drawRect(rect, paint);
            text(canvas, String.format(Locale.US, "%03d", value), x + 214, y + 4, 16, color, Paint.Align.LEFT);
        }

        private void meterBar(Canvas canvas, String label, float x, float y, int value, int color) {
            text(canvas, label, x, y + 4, 13, color, Paint.Align.LEFT);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(color);
            rect.set(x + 72, y - 8, x + 220, y + 4);
            canvas.drawRect(rect, paint);
            paint.setStyle(Paint.Style.FILL);
            rect.set(x + 72, y - 8, x + 72 + 148 * value / 100f, y + 4);
            canvas.drawRect(rect, paint);
            text(canvas, String.format(Locale.US, "%03d", value), x + 238, y + 4, 13, color, Paint.Align.LEFT);
        }

        private void statBox(Canvas canvas, String label, float x, float y, float width) {
            rect.set(x, y, x + width, y + 26);
            box(canvas, rect, true);
            text(canvas, label, x + width / 2f, y + 19, 15, text, Paint.Align.CENTER);
        }

        private void row(Canvas canvas, float x, float y, String label, String value, boolean active) {
            rect.set(x, y, 378, y + 34);
            box(canvas, rect, active);
            text(canvas, label, x + 12, y + 23, 16, active ? text : dim, Paint.Align.LEFT);
            text(canvas, value, 362, y + 23, 14, active ? text : dim, Paint.Align.RIGHT);
        }

        private void pin(Canvas canvas, float x, float y, String label) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 107, 74));
            canvas.drawCircle(x, y, 7, paint);
            text(canvas, label, x + 12, y + 5, 12, text, Paint.Align.LEFT);
        }

        private void box(Canvas canvas, RectF bounds, boolean fill) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill ? faint : Color.TRANSPARENT);
            canvas.drawRect(bounds, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(fill ? text : dim);
            canvas.drawRect(bounds, paint);
        }

        private void text(Canvas canvas, String value, float x, float y, float size, int color, Paint.Align align) {
            textPaint.setShader(null);
            textPaint.setTypeface(font);
            textPaint.setTextSize(size);
            textPaint.setColor(color);
            textPaint.setTextAlign(align);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setFakeBoldText(true);
            textPaint.setColorFilter(null);
            canvas.drawText(value, x, y, textPaint);
        }

        private void drawBitmap(Canvas canvas, Bitmap bitmap, float x, float y, float width, float height, int color) {
            if (bitmap == null) {
                return;
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            rect.set(x, y, x + width, y + height);
            canvas.drawBitmap(bitmap, null, rect, paint);
            paint.setColorFilter(null);
        }

        private void drawScanlines(Canvas canvas) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.argb(42, 0, 0, 0));
            for (int y = 0; y < 450; y += 4) {
                canvas.drawLine(0, y, 450, y, paint);
            }
        }

        private void cycleTheme() {
            theme = (theme + 1) % 3;
            if (theme == 1) {
                text = Color.rgb(255, 191, 82);
                dim = Color.argb(150, 255, 191, 82);
                faint = Color.argb(42, 255, 191, 82);
            } else if (theme == 2) {
                text = Color.rgb(114, 232, 255);
                dim = Color.argb(150, 114, 232, 255);
                faint = Color.argb(42, 114, 232, 255);
            } else {
                text = Color.rgb(119, 255, 114);
                dim = Color.argb(150, 119, 255, 114);
                faint = Color.argb(42, 119, 255, 114);
            }
            invalidate();
        }

        private static Bitmap loadBitmap(Context context, String path) {
            try (InputStream stream = context.getAssets().open(path)) {
                return BitmapFactory.decodeStream(stream);
            } catch (IOException ignored) {
                return null;
            }
        }
    }
}
