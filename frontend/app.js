/**
 * UI: отрисовка поля 2048, клавиатура/свайпы, модалки входа, таблица рекордов.
 */
(function () {
    const cellSize = () => {
        const vw = Math.min(18, (window.innerWidth - 80) / 4 / (window.innerWidth / 100));
        return Math.min(vw * (window.innerWidth / 100), 90);
    };

    function createGridBackground() {
        const container = document.getElementById('grid-bg');
        if (!container) return;
        container.innerHTML = '';
        for (let i = 0; i < 16; i++) {
            const cell = document.createElement('div');
            cell.className = 'cell';
            container.appendChild(cell);
        }
    }

    function renderTiles() {
        const container = document.getElementById('tiles');
        if (!container) return;
        container.innerHTML = '';
        const size = cellSize();
        const gap = 6;
        const grid = Game2048.getGrid();
        for (let r = 0; r < Game2048.SIZE; r++) {
            for (let c = 0; c < Game2048.SIZE; c++) {
                const value = grid[r][c];
                if (value === 0) continue;
                const tile = document.createElement('div');
                tile.className = 'tile tile-' + (value <= 2048 ? value : 'super');
                tile.textContent = value;
                const top = r * (size + gap) + gap;
                const left = c * (size + gap) + gap;
                tile.style.width = size + 'px';
                tile.style.height = size + 'px';
                tile.style.top = top + 'px';
                tile.style.left = left + 'px';
                container.appendChild(tile);
            }
        }
    }

    function updateScores() {
        const scoreEl = document.getElementById('score');
        const bestEl = document.getElementById('best');
        if (scoreEl) scoreEl.textContent = Game2048.getScore();
        if (bestEl) bestEl.textContent = Game2048.getBestScore();
    }

    function setOverlayVisible(el, visible) {
        if (!el) return;
        el.classList.toggle('hidden', !visible);
        el.style.display = visible ? 'flex' : 'none';
    }

    function updateUI() {
        updateScores();
        renderTiles();
        const over = Game2048.isOver();
        setOverlayVisible(document.getElementById('game-over'), over);
        const showWin = Game2048.getMaxTile() >= Game2048.WIN_TILE && !Game2048.isWon();
        setOverlayVisible(document.getElementById('win-overlay'), showWin);
    }

    function handleMove(direction) {
        const moved = Game2048.move(direction);
        if (moved) {
            updateUI();
            if (Game2048.checkGameOver()) {
                updateUI();
            }
        }
    }

    function setupKeyboard() {
        document.addEventListener('keydown', function (e) {
            if (['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].indexOf(e.key) === -1) return;
            e.preventDefault();
            const map = { ArrowLeft: 'left', ArrowRight: 'right', ArrowUp: 'up', ArrowDown: 'down' };
            handleMove(map[e.key]);
        });
    }

    let touchStartX = 0, touchStartY = 0;
    function setupTouch() {
        const gridEl = document.getElementById('grid-container');
        if (!gridEl) return;
        gridEl.addEventListener('touchstart', function (e) {
            touchStartX = e.touches[0].clientX;
            touchStartY = e.touches[0].clientY;
        }, { passive: true });
        gridEl.addEventListener('touchend', function (e) {
            const dx = e.changedTouches[0].clientX - touchStartX;
            const dy = e.changedTouches[0].clientY - touchStartY;
            const min = 30;
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > min) handleMove('right');
                else if (dx < -min) handleMove('left');
            } else {
                if (dy > min) handleMove('down');
                else if (dy < -min) handleMove('up');
            }
        }, { passive: true });
    }

    function showWinOverlay() {
        setOverlayVisible(document.getElementById('win-overlay'), true);
    }

    function startNewGame() {
        Game2048.start();
        setOverlayVisible(document.getElementById('game-over'), false);
        setOverlayVisible(document.getElementById('win-overlay'), false);
        updateUI();
    }

    function setAvatarImage(imgEl, url) {
        if (!imgEl) return;
        if (url) {
            imgEl.src = url + (url.includes('?') ? '&' : '?') + 't=' + Date.now();
            imgEl.classList.remove('hidden');
        } else {
            imgEl.removeAttribute('src');
            imgEl.classList.add('hidden');
        }
    }

    async function refreshProfile() {
        const section = document.getElementById('profile-section');
        const hint = document.getElementById('profile-hint');
        if (!window.api.isAuthenticated()) {
            if (section) section.classList.add('hidden');
            setAvatarImage(document.getElementById('user-avatar'), null);
            setAvatarImage(document.getElementById('profile-avatar'), null);
            return;
        }
        if (section) section.classList.remove('hidden');
        try {
            const profile = await window.api.getProfile();
            if (profile.avatarUrl) {
                setAvatarImage(document.getElementById('user-avatar'), profile.avatarUrl);
                setAvatarImage(document.getElementById('profile-avatar'), profile.avatarUrl);
            } else {
                setAvatarImage(document.getElementById('user-avatar'), null);
                setAvatarImage(document.getElementById('profile-avatar'), null);
            }
            if (hint) {
                const parts = [];
                if (profile.hasSave) parts.push('на сервере есть сохранённая партия');
                hint.textContent = parts.length ? parts.join('; ') : 'Можно сохранить партию на сервер (лаба 5 — REST-хранилище).';
            }
        } catch (_) {
            if (hint) hint.textContent = 'Не удалось загрузить профиль';
        }
    }

    function initAuthUI() {
        const updateAuth = () => {
            const token = window.api.getToken();
            const loggedIn = !!token;
            document.getElementById('btn-login').classList.toggle('hidden', loggedIn);
            document.getElementById('btn-register').classList.toggle('hidden', loggedIn);
            document.getElementById('btn-logout').classList.toggle('hidden', !loggedIn);
            const nameEl = document.getElementById('user-name');
            if (nameEl) {
                nameEl.textContent = window.api.username ? window.api.username : '';
                nameEl.style.display = window.api.username ? 'inline' : 'none';
            }
            refreshProfile();
        };
        updateAuth();
        document.getElementById('btn-logout').addEventListener('click', function () {
            window.api.setToken('');
            window.api.username = '';
            updateAuth();
            loadLeaderboard();
            loadMyRecords();
        });
        document.getElementById('btn-login').addEventListener('click', openModal('Вход', true));
        document.getElementById('btn-register').addEventListener('click', openModal('Регистрация', false));
        document.getElementById('modal-close').addEventListener('click', closeModal);
        document.getElementById('auth-form').addEventListener('submit', async function (e) {
            e.preventDefault();
            const username = document.getElementById('auth-username').value.trim();
            const password = document.getElementById('auth-password').value;
            const errEl = document.getElementById('auth-error');
            errEl.textContent = '';
            try {
                const fn = document.getElementById('modal-title').textContent === 'Регистрация' ? window.api.register : window.api.login;
                const res = await fn.call(window.api, username, password);
                window.api.setToken(res.token);
                window.api.username = res.username;
                closeModal();
                location.reload();
            } catch (err) {
                errEl.textContent = err.message || 'Ошибка';
            }
        });
    }

    function openModal(title, isLogin) {
        return function () {
            document.getElementById('modal-title').textContent = title;
            document.getElementById('auth-username').value = '';
            document.getElementById('auth-password').value = '';
            document.getElementById('auth-error').textContent = '';
            document.getElementById('modal-auth').classList.remove('hidden');
        };
    }

    function closeModal() {
        document.getElementById('modal-auth').classList.add('hidden');
    }

    async function loadLeaderboard() {
        const list = document.getElementById('leaderboard-list');
        if (!list) return;
        try {
            const data = await window.api.getTopScores(15);
            list.innerHTML = (data || []).map((r, i) =>
                `<li><span>${i + 1}. ${r.username}</span><span class="score">${r.score}</span></li>`
            ).join('');
        } catch (_) {
            list.innerHTML = '<li>Не удалось загрузить</li>';
        }
    }

    async function loadMyRecords() {
        const list = document.getElementById('my-records-list');
        if (!list) return;
        if (!window.api.isAuthenticated()) {
            list.innerHTML = '<li>Войдите, чтобы видеть свои игры</li>';
            return;
        }
        try {
            const data = await window.api.getMyRecords();
            const items = Array.isArray(data) ? data : [];
            list.innerHTML = items.length === 0
                ? '<li>Пока нет сохранённых игр</li>'
                : items.map(r =>
                    `<li><span>${Number(r.score)} (${Number(r.maxTile)})</span><span class="score">${r.playedAt ? new Date(r.playedAt).toLocaleDateString() : ''}</span></li>`
                ).join('');
        } catch (err) {
            if (err.status === 401) {
                list.innerHTML = '<li>Войдите, чтобы видеть свои игры</li>';
            } else {
                list.innerHTML = '<li>Ошибка загрузки</li>';
            }
        }
    }

    async function saveRecord() {
        if (!window.api.isAuthenticated()) {
            alert('Войдите, чтобы сохранить результат.');
            return;
        }
        try {
            await window.api.saveRecord(
                Game2048.getScore(),
                Game2048.getMaxTile(),
                Game2048.getMovesCount()
            );
            loadLeaderboard();
            loadMyRecords();
            alert('Результат сохранён.');
        } catch (e) {
            alert(e.message || 'Не удалось сохранить.');
        }
    }

    function init() {
        createGridBackground();
        startNewGame();
        setupKeyboard();
        setupTouch();

        document.getElementById('btn-restart').addEventListener('click', startNewGame);
        document.getElementById('btn-restart-over').addEventListener('click', startNewGame);
        document.getElementById('btn-continue').addEventListener('click', function () {
            Game2048.setWon(true);
            document.getElementById('win-overlay').classList.add('hidden');
        });
        document.getElementById('btn-save').addEventListener('click', saveRecord);

        document.getElementById('avatar-input').addEventListener('change', async function (e) {
            const file = e.target.files && e.target.files[0];
            if (!file || !window.api.isAuthenticated()) return;
            try {
                await window.api.uploadAvatar(file);
                await refreshProfile();
                alert('Аватар загружен.');
            } catch (err) {
                alert(err.message || 'Ошибка загрузки аватара');
            }
            e.target.value = '';
        });

        document.getElementById('btn-save-game').addEventListener('click', async function () {
            if (!window.api.isAuthenticated()) {
                alert('Войдите, чтобы сохранить партию на сервер.');
                return;
            }
            try {
                await window.api.uploadGameSave(Game2048.getState());
                await refreshProfile();
                alert('Партия сохранена на сервере.');
            } catch (err) {
                alert(err.message || 'Не удалось сохранить партию');
            }
        });

        document.getElementById('btn-load-game').addEventListener('click', async function () {
            if (!window.api.isAuthenticated()) {
                alert('Войдите, чтобы загрузить партию.');
                return;
            }
            try {
                const state = await window.api.downloadGameSave();
                if (!state) {
                    alert('Сохранённой партии на сервере нет.');
                    return;
                }
                if (Game2048.loadState(state)) {
                    setOverlayVisible(document.getElementById('game-over'), false);
                    setOverlayVisible(document.getElementById('win-overlay'), false);
                    updateUI();
                    alert('Партия загружена.');
                }
            } catch (err) {
                alert(err.message || 'Не удалось загрузить партию');
            }
        });

        document.getElementById('btn-export-save').addEventListener('click', function () {
            const blob = new Blob([JSON.stringify(Game2048.getState(), null, 2)], { type: 'application/json' });
            const a = document.createElement('a');
            a.href = URL.createObjectURL(blob);
            a.download = 'game2048-save.json';
            a.click();
            URL.revokeObjectURL(a.href);
        });

        document.getElementById('import-save-input').addEventListener('change', function (e) {
            const file = e.target.files && e.target.files[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = function () {
                try {
                    const state = JSON.parse(reader.result);
                    if (Game2048.loadState(state)) {
                        setOverlayVisible(document.getElementById('game-over'), false);
                        setOverlayVisible(document.getElementById('win-overlay'), false);
                        updateUI();
                    }
                } catch (_) {
                    alert('Некорректный файл сохранения');
                }
            };
            reader.readAsText(file);
            e.target.value = '';
        });

        var stored = window.api.getStoredUser();
        if (stored) window.api.username = stored.username;
        initAuthUI();
        loadLeaderboard();
        loadMyRecords();

        window.addEventListener('resize', function () {
            renderTiles();
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.game2048App = { updateUI, showWinOverlay, startNewGame };
})();
