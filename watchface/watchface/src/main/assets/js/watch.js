(function () {
    const views = ["status", "inventory", "data", "map", "radio"];
    const titles = {
        status: "STAT",
        inventory: "INV",
        data: "DATA",
        map: "MAP",
        radio: "RADIO"
    };
    const themes = ["green", "amber", "blue"];
    const inventory = [
        { name: "10MM SIDEARM", icon: "img/ico/gun.png", dmg: 18, ammo: 64, wt: 4.2, val: 53 },
        { name: "STIM KIT", icon: "img/power.png", dmg: 0, ammo: 3, wt: 0.1, val: 48 },
        { name: "FIELD ARMOR", icon: "img/helmet.png", dmg: 22, ammo: 1, wt: 8.7, val: 126 },
        { name: "RAD FILTER", icon: "img/ico/radioactive.png", dmg: 0, ammo: 2, wt: 0.4, val: 32 }
    ];
    const logs = {
        signal: "Follow the carrier tone north-east. Keep transmitter gain under 60%.",
        supply: "Three water cells remain. Return through the service road before dusk.",
        vault: "Cache ping recovered. Local lock accepts a six pulse handshake."
    };

    let viewIndex = 0;
    let themeIndex = 0;
    let touchStartX = 0;
    let touchStartY = 0;
    let selectedItem = 0;
    const initialHash = window.location.hash.slice(1);
    const watchFaceMode = initialHash === "face" || initialHash === "facebg";

    const bootScreen = document.getElementById("bootScreen");
    const bootText = document.getElementById("bootText");
    const watch = document.getElementById("pipWatch");
    const viewTitle = document.getElementById("viewTitle");
    const themeToggle = document.getElementById("themeToggle");
    const inventoryList = document.getElementById("inventoryList");
    const itemDetail = document.getElementById("itemDetail");
    const faceTime = document.getElementById("faceTime");
    const faceDate = document.getElementById("faceDate");

    if (watchFaceMode) {
        document.body.classList.add("face-mode");
        if (initialHash === "facebg") {
            document.body.classList.add("face-background");
        }
    }

    function boot() {
        const lines = [
            "VAULTWATCH BIOS 7.4",
            "MEMORY OK",
            "WRIST DISPLAY 396/450 READY",
            "GEIGER SENSOR ONLINE",
            "TACTICAL MAP CACHE OK",
            "LOADING VAULTWATCH..."
        ];
        let line = 0;
        let char = 0;
        const timer = window.setInterval(function () {
            if (line >= lines.length) {
                window.clearInterval(timer);
                bootScreen.classList.add("hidden");
                watch.classList.add("ready");
                return;
            }
            bootText.textContent += lines[line].charAt(char);
            char += 1;
            if (char > lines[line].length) {
                bootText.textContent += "\n";
                line += 1;
                char = 0;
            }
        }, 18);
    }

    function setView(nextView) {
        const index = views.indexOf(nextView);
        if (index === -1) {
            return;
        }
        viewIndex = index;
        document.querySelectorAll("[data-view-panel]").forEach(function (panel) {
            panel.classList.toggle("active", panel.dataset.viewPanel === nextView);
        });
        document.querySelectorAll(".tab-button").forEach(function (button) {
            button.classList.toggle("active", button.dataset.view === nextView);
        });
        viewTitle.textContent = titles[nextView];
        if (!watchFaceMode && window.location.hash.slice(1) !== nextView) {
            window.history.replaceState(null, "", "#" + nextView);
        }
    }

    function stepView(delta) {
        const next = (viewIndex + delta + views.length) % views.length;
        setView(views[next]);
    }

    function setTheme(index) {
        themeIndex = (index + themes.length) % themes.length;
        const theme = themes[themeIndex];
        document.body.className = "theme-" + theme;
        themeToggle.textContent = theme.toUpperCase();
    }

    function renderInventory() {
        inventoryList.innerHTML = "";
        inventory.forEach(function (item, index) {
            const button = document.createElement("button");
            button.type = "button";
            button.innerHTML = '<img src="' + item.icon + '" alt=""> <span>' + item.name + '</span>';
            button.className = index === selectedItem ? "active" : "";
            button.addEventListener("click", function () {
                selectedItem = index;
                renderInventory();
            });
            inventoryList.appendChild(button);
        });

        const item = inventory[selectedItem];
        itemDetail.innerHTML = [
            '<img class="detail-icon" src="' + item.icon + '" alt="">',
            "<strong>" + item.name + "</strong>",
            "<span>DMG " + String(item.dmg).padStart(3, "0") + "</span>",
            "<span>COUNT " + String(item.ammo).padStart(3, "0") + "</span>",
            "<span>WT " + item.wt.toFixed(1) + "</span>",
            "<span>VAL " + String(item.val).padStart(3, "0") + "</span>"
        ].join("");
    }

    function updateClock() {
        const now = new Date();
        document.getElementById("watchTime").textContent = now.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
        });
        document.getElementById("watchDate").textContent = String(now.getMonth() + 1).padStart(2, "0") + "/" + String(now.getDate()).padStart(2, "0");

        if (faceTime) {
            faceTime.textContent = now.toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit"
            });
        }
        if (faceDate) {
            faceDate.textContent = now.toLocaleDateString([], {
                weekday: "short",
                month: "2-digit",
                day: "2-digit"
            }).toUpperCase();
        }

        const battery = Math.max(41, 100 - (now.getMinutes() % 60));
        document.getElementById("batteryValue").textContent = "BAT " + battery;
    }

    function updateNoise() {
        const signal = 68 + Math.floor(Math.random() * 17);
        const rads = 12 + Math.floor(Math.random() * 17);
        document.getElementById("noiseValue").textContent = "SIG " + signal;
        document.getElementById("radValue").textContent = String(rads).padStart(3, "0");
        document.getElementById("radBar").style.width = rads + "%";
    }

    function bindEvents() {
        document.querySelectorAll(".tab-button").forEach(function (button) {
            button.addEventListener("click", function () {
                setView(button.dataset.view);
            });
        });

        document.getElementById("prevView").addEventListener("click", function () {
            stepView(-1);
        });
        document.getElementById("nextView").addEventListener("click", function () {
            stepView(1);
        });
        themeToggle.addEventListener("click", function () {
            setTheme(themeIndex + 1);
        });

        document.querySelectorAll(".quest-row").forEach(function (row) {
            row.addEventListener("click", function () {
                document.querySelectorAll(".quest-row").forEach(function (item) {
                    item.classList.remove("active");
                });
                row.classList.add("active");
                document.getElementById("terminalNote").textContent = logs[row.dataset.log];
            });
        });

        document.querySelectorAll(".map-pin").forEach(function (pin) {
            pin.addEventListener("click", function () {
                document.querySelectorAll(".map-pin").forEach(function (item) {
                    item.classList.remove("active");
                });
                pin.classList.add("active");
                document.getElementById("mapReadout").textContent = pin.dataset.location + " | LOCKED | LOCAL";
            });
        });

        document.querySelectorAll(".station").forEach(function (station) {
            station.addEventListener("click", function () {
                document.querySelectorAll(".station").forEach(function (item) {
                    item.classList.remove("active");
                });
                station.classList.add("active");
                document.getElementById("nowPlaying").textContent = station.dataset.station + " | " + station.dataset.track;
            });
        });

        window.addEventListener("keydown", function (event) {
            if (event.key === "ArrowRight") {
                stepView(1);
            }
            if (event.key === "ArrowLeft") {
                stepView(-1);
            }
            if (event.key === "t" || event.key === "T") {
                setTheme(themeIndex + 1);
            }
        });

        window.addEventListener("wheel", function (event) {
            if (Math.abs(event.deltaY) > 10) {
                stepView(event.deltaY > 0 ? 1 : -1);
            }
        }, { passive: true });

        window.addEventListener("touchstart", function (event) {
            const touch = event.changedTouches[0];
            touchStartX = touch.clientX;
            touchStartY = touch.clientY;
        }, { passive: true });

        window.addEventListener("touchend", function (event) {
            const touch = event.changedTouches[0];
            const dx = touch.clientX - touchStartX;
            const dy = touch.clientY - touchStartY;
            if (Math.abs(dx) > 42 && Math.abs(dx) > Math.abs(dy)) {
                stepView(dx < 0 ? 1 : -1);
            }
        }, { passive: true });
    }

    renderInventory();
    bindEvents();
    setView(views.includes(initialHash) ? initialHash : "status");
    updateClock();
    updateNoise();
    boot();

    window.setInterval(updateClock, 1000);
    window.setInterval(updateNoise, 2400);
}());
