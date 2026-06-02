/**
 * Логика игры 2048: поле 4x4, сдвиги, слияние плиток, победа/проигрыш.
 */
const Game2048 = (function () {
    const SIZE = 4;
    const WIN_TILE = 2048;

    let grid = [];
    let score = 0;
    let bestScore = 0;
    let movesCount = 0;
    let won = false;
    let over = false;
    let maxTile = 0;

    function initGrid() {
        grid = Array(SIZE).fill(null).map(() => Array(SIZE).fill(0));
        score = 0;
        movesCount = 0;
        won = false;
        over = false;
        maxTile = 0;
        bestScore = Math.max(bestScore, score);
        try {
            const saved = localStorage.getItem('game2048_best');
            if (saved) bestScore = Math.max(bestScore, parseInt(saved, 10));
        } catch (_) {}
    }

    function emptyCells() {
        const list = [];
        for (let r = 0; r < SIZE; r++) {
            for (let c = 0; c < SIZE; c++) {
                if (grid[r][c] === 0) list.push({ r, c });
            }
        }
        return list;
    }

    function addRandomTile() {
        const cells = emptyCells();
        if (cells.length === 0) return false;
        const { r, c } = cells[Math.floor(Math.random() * cells.length)];
        grid[r][c] = Math.random() < 0.9 ? 2 : 4;
        if (grid[r][c] > maxTile) maxTile = grid[r][c];
        return true;
    }

    function slideRow(row) {
        let moved = false;
        const line = row.filter(x => x !== 0);
        const merged = [];
        let i = 0;
        while (i < line.length) {
            if (i + 1 < line.length && line[i] === line[i + 1]) {
                merged.push(line[i] * 2);
                score += line[i] * 2;
                i += 2;
                moved = true;
            } else {
                merged.push(line[i]);
                i += 1;
            }
        }
        while (merged.length < SIZE) merged.push(0);
        for (let j = 0; j < SIZE; j++) {
            if (row[j] !== merged[j]) moved = true;
            row[j] = merged[j];
        }
        return moved;
    }

    function rotate() {
        const next = Array(SIZE).fill(null).map(() => Array(SIZE).fill(0));
        for (let r = 0; r < SIZE; r++) {
            for (let c = 0; c < SIZE; c++) {
                next[c][SIZE - 1 - r] = grid[r][c];
            }
        }
        grid = next;
    }

    function move(direction) {
        if (over) return false;
        let moved = false;
        if (direction === 'left') {
            for (let r = 0; r < SIZE; r++) {
                if (slideRow(grid[r])) moved = true;
            }
        } else if (direction === 'right') {
            for (let r = 0; r < SIZE; r++) {
                grid[r].reverse();
                if (slideRow(grid[r])) moved = true;
                grid[r].reverse();
            }
        } else if (direction === 'up') {
            rotate();
            rotate();
            rotate();
            for (let r = 0; r < SIZE; r++) {
                if (slideRow(grid[r])) moved = true;
            }
            rotate();
        } else if (direction === 'down') {
            rotate();
            for (let r = 0; r < SIZE; r++) {
                if (slideRow(grid[r])) moved = true;
            }
            rotate();
            rotate();
            rotate();
        }
        if (moved) {
            movesCount++;
            for (let r = 0; r < SIZE; r++) {
                for (let c = 0; c < SIZE; c++) {
                    if (grid[r][c] > maxTile) maxTile = grid[r][c];
                }
            }
            addRandomTile();
            if (score > bestScore) {
                bestScore = score;
                try {
                    localStorage.setItem('game2048_best', String(bestScore));
                } catch (_) {}
            }
        }
        return moved;
    }

    function canMove() {
        for (let r = 0; r < SIZE; r++) {
            for (let c = 0; c < SIZE; c++) {
                if (grid[r][c] === 0) return true;
                if (c < SIZE - 1 && grid[r][c] === grid[r][c + 1]) return true;
                if (r < SIZE - 1 && grid[r][c] === grid[r + 1][c]) return true;
            }
        }
        return false;
    }

    function checkGameOver() {
        if (emptyCells().length > 0) return false;
        over = !canMove();
        return over;
    }

    function getState() {
        return {
            grid: grid.map(row => [...row]),
            score,
            bestScore,
            movesCount,
            won,
            over,
            maxTile
        };
    }

    function start() {
        initGrid();
        addRandomTile();
        addRandomTile();
        return getState();
    }

    function loadState(state) {
        if (!state || !Array.isArray(state.grid)) return false;
        grid = state.grid.map(row => row.map(v => Number(v) || 0));
        score = Number(state.score) || 0;
        bestScore = Number(state.bestScore) || bestScore;
        movesCount = Number(state.movesCount) || 0;
        won = !!state.won;
        over = !!state.over;
        maxTile = Number(state.maxTile) || 0;
        for (let r = 0; r < SIZE; r++) {
            for (let c = 0; c < SIZE; c++) {
                if (grid[r][c] > maxTile) maxTile = grid[r][c];
            }
        }
        try {
            localStorage.setItem('game2048_best', String(Math.max(bestScore, score)));
        } catch (_) {}
        return true;
    }

    function setWon(value) {
        won = value;
    }

    function getScore() { return score; }
    function getBestScore() { return bestScore; }
    function getMovesCount() { return movesCount; }
    function getMaxTile() { return maxTile; }
    function isOver() { return over; }
    function isWon() { return won; }
    function getGrid() { return grid.map(row => [...row]); }

    return {
        SIZE,
        WIN_TILE,
        start,
        move,
        checkGameOver,
        getState,
        loadState,
        setWon,
        getScore,
        getBestScore,
        getMovesCount,
        getMaxTile,
        isOver,
        isWon,
        getGrid
    };
})();
