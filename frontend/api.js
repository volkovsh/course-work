/**
 * API клиент для взаимодействия с backend (Spring Boot).
 * Базовый URL задаётся через window.API_BASE_URL или по умолчанию /api.
 */
(function () {
    const BASE = window.API_BASE_URL || '/api';

    function getToken() {
        return localStorage.getItem('token');
    }

    function parseJwtPayload(token) {
        try {
            const base64url = token.split('.')[1];
            if (!base64url) return null;

            // JWT uses base64url (RFC 7515) and JSON is UTF-8.
            let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
            const pad = base64.length % 4;
            if (pad) base64 += '='.repeat(4 - pad);

            const binary = atob(base64);
            const bytes = new Uint8Array(binary.length);
            for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
            const json = new TextDecoder('utf-8').decode(bytes);
            return JSON.parse(json);
        } catch (_) {
            return null;
        }
    }

    function headers(includeAuth = false, json = true) {
        const h = { 'Accept': 'application/json' };
        if (json) {
            h['Content-Type'] = 'application/json';
        }
        const token = getToken();
        if (includeAuth && token) {
            h['Authorization'] = 'Bearer ' + token;
        }
        return h;
    }

    async function request(method, path, body, auth = false) {
        const url = BASE + path;
        const options = {
            method,
            headers: headers(auth, body != null || method === 'POST' || method === 'PUT')
        };
        if (body != null) {
            options.body = JSON.stringify(body);
        }
        const res = await fetch(url, options);
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
            const err = new Error(data.error || data.message || res.statusText || 'Ошибка сети');
            err.status = res.status;
            err.data = data;
            throw err;
        }
        return data;
    }

    async function requestRaw(method, path, body, contentType, auth = true) {
        const url = BASE + path;
        const h = headers(auth, false);
        if (contentType) {
            h['Content-Type'] = contentType;
        }
        const res = await fetch(url, { method, headers: h, body });
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            const err = new Error(data.error || res.statusText || 'Ошибка сети');
            err.status = res.status;
            throw err;
        }
        if (res.status === 204) return null;
        const ct = res.headers.get('Content-Type') || '';
        if (ct.includes('application/json')) {
            return res.json();
        }
        return res;
    }

    window.api = {
        async register(username, password) {
            return request('POST', '/auth/register', { username, password });
        },
        async login(username, password) {
            return request('POST', '/auth/login', { username, password });
        },
        async saveRecord(score, maxTile, movesCount) {
            return request('POST', '/records', { score, maxTile, movesCount: movesCount ?? null }, true);
        },
        async getTopScores(limit = 20) {
            return request('GET', '/records/top?limit=' + (limit || 20));
        },
        async getMyRecords() {
            return request('GET', '/records/my', null, true);
        },
        async getProfile() {
            return request('GET', '/profile', null, true);
        },
        async uploadAvatar(file) {
            const form = new FormData();
            form.append('file', file);
            const url = BASE + '/files/avatars/me';
            const h = { 'Accept': 'application/json' };
            const token = getToken();
            if (token) h['Authorization'] = 'Bearer ' + token;
            const res = await fetch(url, { method: 'POST', headers: h, body: form });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) {
                throw new Error(data.error || res.statusText || 'Не удалось загрузить аватар');
            }
            return data;
        },
        async uploadGameSave(state) {
            return request('PUT', '/files/saves/me', state, true);
        },
        async downloadGameSave() {
            const url = BASE + '/files/saves/me';
            const h = headers(true, false);
            const res = await fetch(url, { method: 'GET', headers: h });
            if (res.status === 404) return null;
            if (!res.ok) {
                throw new Error('Не удалось загрузить сохранение');
            }
            return res.json();
        },
        isAuthenticated() {
            return !!getToken();
        },
        getStoredUser() {
            const token = getToken();
            if (!token) return null;
            const payload = parseJwtPayload(token);
            if (!payload) return null;
            return { username: payload.sub, userId: payload.userId };
        },
        setToken(token) {
            if (token) localStorage.setItem('token', token);
            else localStorage.removeItem('token');
        },
        getToken
    };
})();
