/* =========================================================
   Smart Study AI - theme-script.js (개선판)
   - 테마 스와치 미리보기 + 활성 뱃지
   - 배경 패턴 3가지만 (노트줄·도트·그리드)
   - 스타일 설정 → 고급 설정으로 접기/펼치기
   - 포레스트 그린 테마 추가
   ========================================================= */

(function () {
  'use strict';

  // ── 1. 기본값 ────────────────────────────────────────────
  const DEFAULT_STATE = {
    theme: 'library',
    mode: 'light',
    bg: {
      type: 'solid',
      color1: '#fff4f0',
      color2: '#fff4f0',
      pattern: 'paper'
    },
    widgets: {
      timer:   { on: true,  order: 1, wide: false },
      dday:    { on: true,  order: 2, wide: false },
      heatmap: { on: true,  order: 3, wide: false },
      quiz:    { on: false, order: 4, wide: false },
      memo:    { on: true,  order: 5, wide: false }
    },
    columns: 2,
    align: 'center',
    timer: { defaultMinutes: 25 },
    ddays: [
      { id: 1, name: '모의고사', date: '2026-06-08' },
      { id: 2, name: '토익 시험', date: '2026-06-30' },
      { id: 3, name: '기말고사', date: '2026-07-24' }
    ],
    details: {
      accentColor:     '#e85d4a',
      brandIconColor:  '#14b8a6',
      cardBgColor:     '#ffffff',
      cardStyle:       'shadow',
      btnStyle:        'solid',
      btnColor:        '',
      fontSize:        16,
      radius:          14,
      shadow:          2,
      textHeading:     '#3d1a14',
      textBody:        '#3d1a14',
      textSub:         '#8a4039',
      textBtn:         '#ffffff'
    },
  };

  // ── 2. 테마 프리셋 ───────────────────────────────────────
  const THEME_PRESETS = {
    library: {
      name: '선셋 코랄',
      bg: { type: 'solid', color1: '#fff4f0', color2: '#fff4f0', pattern: 'paper' },
      details: {
        accentColor: '#e85d4a', brandIconColor: '#e85d4a', cardBgColor: '#fff9f7',
        cardStyle: 'shadow', btnStyle: 'solid',
        textHeading: '#3d1a14', textBody: '#3d1a14',
        textSub: '#8a4039', textBtn: '#3d1a14'
      }
    },
    night: {
      name: '스카이 블루',
      bg: { type: 'gradient', color1: '#D6E8EE', color2: '#97CADB', pattern: 'dots' },
      details: {
        accentColor: '#018ABE', brandIconColor: '#018ABE', cardBgColor: '#ffffff',
        cardStyle: 'shadow', btnStyle: 'solid',
        textHeading: '#001B48', textBody: '#02457A',
        textSub: '#018ABE', textBtn: '#001B48'
      }
    },
    cafe: {
      name: '카페 라떼',
      bg: { type: 'solid', color1: '#efe1ce', color2: '#c6a07a', pattern: 'dots' },
      details: {
        accentColor: '#b07a4a', brandIconColor: '#b07a4a', cardBgColor: '#fff9ef',
        cardStyle: 'shadow', btnStyle: 'pill',
        textHeading: '#3a2918', textBody: '#3a2918',
        textSub: '#6a4f3b', textBtn: '#3a2918'
      }
    },
    pastel: {
      name: '체리 블라썸',
      bg: { type: 'gradient', color1: '#ffeef6', color2: '#c7e2ff', pattern: 'dots' },
      details: {
        accentColor: '#d486a8', brandIconColor: '#d486a8', cardBgColor: '#ffffff',
        cardStyle: 'glass', btnStyle: 'pill',
        textHeading: '#4b3a48', textBody: '#4b3a48',
        textSub: '#8a7385', textBtn: '#4b3a48'
      }
    },
    dark: {
      name: '다크 퍼플',
      bg: { type: 'solid', color1: '#14131b', color2: '#2b1f4a', pattern: 'grid' },
      details: {
        accentColor: '#a78bfa', brandIconColor: '#a78bfa', cardBgColor: '#211d2e',
        cardStyle: 'shadow', btnStyle: 'outline',
        textHeading: '#e8e6f5', textBody: '#e8e6f5',
        textSub: '#a9a3c4', textBtn: '#e8e6f5'
      }
    },
    forest: {
      name: '포레스트 그린',
      bg: { type: 'solid', color1: '#e8f5e9', color2: '#c8e6c9', pattern: 'grid' },
      details: {
        accentColor: '#2e7d32', brandIconColor: '#2e7d32', cardBgColor: '#f9fbe7',
        cardStyle: 'shadow', btnStyle: 'solid',
        textHeading: '#1b3a1c', textBody: '#1b3a1c',
        textSub: '#4a7c4e', textBtn: '#1b3a1c'
      }
    },
  };

  const STORAGE_KEY = 'smartStudyAI.dashboard.v2';
  let state = loadState();

  // ── 3. 유틸 ─────────────────────────────────────────────
  function deepClone(obj) { return JSON.parse(JSON.stringify(obj)); }

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return deepClone(DEFAULT_STATE);
      const s = mergeDeep(deepClone(DEFAULT_STATE), JSON.parse(raw));
      if (s.widgets.quick) delete s.widgets.quick;
      return s;
    } catch (e) { return deepClone(DEFAULT_STATE); }
  }

  function saveState() {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
    catch (e) { console.warn('저장 실패:', e); }
  }

  function mergeDeep(target, src) {
    for (const k in src) {
      if (src[k] && typeof src[k] === 'object' && !Array.isArray(src[k])) {
        target[k] = mergeDeep(target[k] || {}, src[k]);
      } else { target[k] = src[k]; }
    }
    return target;
  }

  // ── 4. DOM 캐시 ─────────────────────────────────────────
  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => document.querySelectorAll(sel);

  const els = {
    body:          document.body,
    studyRoom:     $('#studyRoom'),
    widgets:       $('#widgets'),
    bgColor1:        $('#bgColor1'),
    bgColor2:        $('#bgColor2'),
    accentColor:     $('#accentColor'),
    brandIconColor:  $('#brandIconColor'),
    cardBgColor:   $('#cardBgColor'),
    cardStyle:     $('#cardStyle'),
    btnStyle:      $('#btnStyle'),
    btnColor:      $('#btnColor'),
    fontSize:      $('#fontSize'),
    fontSizeVal:   $('#fontSizeVal'),
    radius:        $('#radius'),
    radiusVal:     $('#radiusVal'),
    shadow:        $('#shadow'),
    shadowVal:     $('#shadowVal'),
    textHeading:   $('#textHeading'),
    textBody:      $('#textBody'),
    textSub:       $('#textSub'),
    textBtn:       $('#textBtn'),
    saveBtn:       $('#saveBtn'),
    resetBtn:      $('#resetBtn'),
    heatmap:       $('#heatmap'),
    activeName:    $('#activeName')
  };

  // ── 5. 고급 설정 토글 ────────────────────────────────────
  let advancedOpen = false;
  window.toggleAdvanced = function () {
    advancedOpen = !advancedOpen;
    const body  = $('#advancedBody');
    const arrow = $('#advancedArrow');
    body.classList.toggle('open', advancedOpen);
    arrow.style.transform = advancedOpen ? 'rotate(180deg)' : '';
  };

  // ── 6. 테마 스와치 선택 ──────────────────────────────────
  window.pickTheme = function (el) {
    const key    = el.dataset.theme;
    const preset = THEME_PRESETS[key];
    if (!preset) return;

    // state 업데이트 — buttons·ddays는 사용자 커스텀값 유지
    const savedButtons = state.buttons;
    const savedDdays   = state.ddays;

    state.theme   = key;
    state.bg      = { ...state.bg,      ...preset.bg };
    state.details = { ...state.details, ...preset.details, btnColor: '' };

    state.buttons = savedButtons;
    state.ddays   = savedDdays;

    applyState();
    saveState();
  };

  function updateSwatchUI() {
    $$('.swatch-item').forEach(el => {
      el.classList.toggle('active', el.dataset.theme === state.theme);
    });
    // 활성 뱃지 이름 갱신
    const preset = THEME_PRESETS[state.theme];
    if (els.activeName && preset) {
      els.activeName.textContent = preset.name;
    }
    // 뱃지 색상도 accent 에 맞게
    const badge = $('#activeBadge');
    if (badge) {
      badge.style.background = mixWithWhite(state.details.accentColor, 0.8);
      badge.style.color      = state.details.accentColor;
    }
  }

  // ── 7. 적용 함수들 ───────────────────────────────────────

  function applyTheme() {
    els.body.setAttribute('data-theme', state.theme);
    updateSwatchUI();
  }

  function applyBackground() {
    // 'pattern' 타입은 더 이상 사용하지 않으므로 'solid'로 정규화
    if (state.bg.type === 'pattern') state.bg.type = 'solid';
    const { type, color1, color2 } = state.bg;
    els.studyRoom.setAttribute('data-bg-mode', type);
    els.studyRoom.style.setProperty('--room-bg',  color1);
    els.studyRoom.style.setProperty('--room-bg2', color2);

    els.bgColor1.value = color1;
    if (els.bgColor2) {
      els.bgColor2.value = color2;
    }

    // 단색/그라데이션 세그먼트 버튼 active 동기화
    $$('#bgTypeSeg .seg-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.bg === type);
    });

    // 보조색 행: 그라데이션일 때만 표시
    const color2Row = $('#bgColor2Row');
    if (color2Row) {
      color2Row.style.display = type === 'gradient' ? '' : 'none';
    }

    applyBgPreview();
  }

  function applyWidgets() {
    const cols = state.columns || 2;
    const container = els.widgets;
    const sorted = Object.entries(state.widgets)
      .sort((a, b) => a[1].order - b[1].order);

    // 1차: DOM 순서 맞추기
    sorted.forEach(([key]) => {
      const el = container.querySelector(`.widget[data-widget-el="${key}"]`);
      if (el) container.appendChild(el);
    });

    // 2차: 명시적 gridRow/gridColumn 배치 (span 2 빈 셀 버그 방지)
    let row = 1, col = 1;
    sorted.forEach(([key, w]) => {
      const el = container.querySelector(`.widget[data-widget-el="${key}"]`);
      if (!el) return;
      el.classList.toggle('hidden', !w.on);
      el.style.order = '';
      if (!w.on) { el.style.gridRow = ''; el.style.gridColumn = ''; return; }
      const wide = w.wide && cols >= 2;
      if (wide && col > 1) { row++; col = 1; }
      el.style.gridRow = String(row);
      el.style.gridColumn = wide ? '1 / -1' : String(col);
      if (wide) { row++; col = 1; }
      else { col++; if (col > cols) { col = 1; row++; } }
    });

    container.setAttribute('data-align', state.align);
    container.setAttribute('data-columns', String(cols));
  }

  function applyTimer() {
    const el = document.querySelector('.timer-display');
    if (!el) return;
    const mins = state.timer?.defaultMinutes ?? 25;
    el.textContent = String(mins).padStart(2, '0') + ':00';
  }

  function applyDdays() {
    const list = document.querySelector('.dday-list');
    if (!list || !state.ddays?.length) return;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    list.innerHTML = state.ddays.map(d => {
      let label = '날짜 미설정';
      if (d.date) {
        const diff = Math.ceil((new Date(d.date) - today) / 86400000);
        label = diff > 0 ? `D-${diff}` : diff === 0 ? 'D-Day' : `D+${Math.abs(diff)}`;
      }
      return `<li><span>${d.name || '(이름 없음)'}</span><b>${label}</b></li>`;
    }).join('');
  }

  function applyButtons() {
    const b = state.buttons || {};
    const startBtn = document.querySelector('#studyRoom .widget[data-widget-el="timer"] .btn-primary');
    const resetBtn = document.querySelector('#studyRoom .widget[data-widget-el="timer"] .btn-ghost');
    if (startBtn) startBtn.textContent = b.timerStartLabel || '시작';
    if (resetBtn) resetBtn.textContent = b.timerResetLabel || '리셋';
    const navDefaults = ['대시보드', '학습 기록', '설정'];
    ['nav1', 'nav2', 'nav3'].forEach((key, i) => {
      const el = document.querySelector(`#studyRoom [data-nav="${key}"]`);
      if (el) el.textContent = b[key] || navDefaults[i];
    });
  }

  function applyDetails() {
    const d = state.details;
    els.body.style.setProperty('--accent',            d.accentColor);
    els.body.style.setProperty('--accent-soft',       mixWithWhite(d.accentColor, 0.85));
    els.body.style.setProperty('--brand-icon-color',  d.brandIconColor || '#14b8a6');
    els.body.style.setProperty('--card-bg',     d.cardBgColor);
    els.body.style.setProperty('--preview-font-size', d.fontSize + 'px');
    els.body.style.setProperty('--card-radius', d.radius + 'px');
    els.body.style.setProperty('--card-shadow', shadowToken(d.shadow));
    els.body.setAttribute('data-card-style', d.cardStyle);
    els.body.setAttribute('data-btn-style',  d.btnStyle);
    if (d.btnColor) {
      els.body.style.setProperty('--btn-primary-bg', d.btnColor);
    } else {
      els.body.style.removeProperty('--btn-primary-bg');
    }
    els.studyRoom.style.setProperty('--preview-text-heading', d.textHeading);
    els.studyRoom.style.setProperty('--preview-text-body',    d.textBody);
    els.studyRoom.style.setProperty('--preview-text-sub',     d.textSub);
    els.studyRoom.style.setProperty('--preview-text-btn',     d.textBtn);

    // 테마 미리보기(bgDemoArea)에도 동일하게 적용
    const _demo = $('#bgDemoArea');
    if (_demo) {
      _demo.style.setProperty('--preview-text-heading', d.textHeading);
      _demo.style.setProperty('--preview-text-body',    d.textBody);
      _demo.style.setProperty('--preview-text-sub',     d.textSub);
      _demo.style.setProperty('--card-shadow',          shadowToken(d.shadow));
      _demo.style.setProperty('--card-radius',          d.radius + 'px');
      _demo.style.setProperty('--card-bg',              d.cardBgColor);
      // CSS 변수 상속이 반영되지 않는 경우를 대비해 직접 설정
      const demoShadow = d.cardStyle === 'flat' ? 'none' : shadowToken(d.shadow);
      $$('#bgDemoArea .bg-demo-card').forEach(card => {
        card.style.boxShadow = demoShadow;
        card.style.borderRadius = d.radius + 'px';
      });
    }

    els.accentColor.value    = d.accentColor;
    if (els.brandIconColor) els.brandIconColor.value = d.brandIconColor || '#14b8a6';
    els.cardBgColor.value  = d.cardBgColor;
    els.cardStyle.value    = d.cardStyle;
    els.btnStyle.value     = d.btnStyle;
    if (els.btnColor) els.btnColor.value = d.btnColor || d.accentColor;
    els.fontSize.value     = d.fontSize;
    els.radius.value       = d.radius;
    els.shadow.value       = d.shadow;
    els.textHeading.value  = d.textHeading;
    els.textBody.value     = d.textBody;
    els.textSub.value      = d.textSub;
    els.textBtn.value      = d.textBtn;
    els.fontSizeVal.textContent = d.fontSize + 'px';
    els.radiusVal.textContent   = d.radius   + 'px';
    els.shadowVal.textContent   = d.shadow;
    applyBgPreview();
  }

  function shadowToken(level) {
    const map = {
      0: 'none',
      1: '0 2px 8px rgba(0,0,0,0.08)',
      2: '0 4px 16px rgba(0,0,0,0.13)',
      3: '0 8px 24px rgba(0,0,0,0.18)',
      4: '0 12px 32px rgba(0,0,0,0.23)',
      5: '0 16px 40px rgba(0,0,0,0.30)'
    };
    return map[level] ?? map[2];
  }

  function mixColors(hex1, hex2, t) {
    const p = h => {
      const c = h.replace('#','');
      return [parseInt(c.slice(0,2),16), parseInt(c.slice(2,4),16), parseInt(c.slice(4,6),16)];
    };
    const [r1,g1,b1] = p(hex1), [r2,g2,b2] = p(hex2);
    const toHex = n => Math.max(0,Math.min(255,Math.round(n))).toString(16).padStart(2,'0');
    return '#' + toHex(r1+(r2-r1)*t) + toHex(g1+(g2-g1)*t) + toHex(b1+(b2-b1)*t);
  }

  function applyMode() {
    const mode = state.mode || 'light';
    els.body.setAttribute('data-mode', mode);
    $$('#modeToggle .mode-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.mode === mode);
    });
  }

  function applyBgPreview() {
    const { color1, color2, type } = state.bg;
    const demoArea = $('#bgDemoArea');
    if (!demoArea) return;

    demoArea.style.background = type === 'gradient'
      ? `linear-gradient(135deg, ${color1}, ${color2})`
      : color1;

    // 컬러 & 스타일 변경 사항을 테마 미리보기에 반영
    demoArea.setAttribute('data-card-style', state.details.cardStyle || 'shadow');
    demoArea.setAttribute('data-btn-style',  state.details.btnStyle  || 'solid');
  }

  function mixWithWhite(hex, ratio) {
    const c = hex.replace('#','');
    const r = parseInt(c.substring(0,2),16);
    const g = parseInt(c.substring(2,4),16);
    const b = parseInt(c.substring(4,6),16);
    const mix   = (ch) => Math.round(ch + (255 - ch) * ratio);
    const toHex = (n)  => n.toString(16).padStart(2,'0');
    return '#' + toHex(mix(r)) + toHex(mix(g)) + toHex(mix(b));
  }

  function renderHeatmap() {
    if (!els.heatmap) return;
    els.heatmap.innerHTML = '';
    for (let i = 0; i < 70; i++) {
      const cell = document.createElement('span');
      const r = Math.random();
      if      (r > 0.85) cell.className = 'lv4';
      else if (r > 0.65) cell.className = 'lv3';
      else if (r > 0.4)  cell.className = 'lv2';
      else if (r > 0.2)  cell.className = 'lv1';
      els.heatmap.appendChild(cell);
    }
  }

  function applyState() {
    applyTheme();
    applyBackground();
    applyWidgets();
    applyDetails();
    updateSwatchUI();
    applyTimer();
    applyDdays();
    applyButtons();
    applyBgPreview(); // CSS 변수 갱신 후 테마 미리보기 최종 동기화
  }

  // ── 9. 이벤트 바인딩 ─────────────────────────────────────

  els.bgColor1.addEventListener('input', (e) => {
    state.bg.color1 = e.target.value;
    applyBackground(); saveState();
  });
  els.bgColor2.addEventListener('input', (e) => {
    state.bg.color2 = e.target.value;
    applyBackground(); saveState();
  });

  // 단색 / 그라데이션 토글
  const bgTypeSeg = $('#bgTypeSeg');
  if (bgTypeSeg) {
    bgTypeSeg.addEventListener('click', (e) => {
      const btn = e.target.closest('.seg-btn');
      if (!btn) return;
      state.bg.type = btn.dataset.bg;
      applyBackground(); saveState();
    });
  }

  els.accentColor.addEventListener('input', (e) => {
    state.details.accentColor = e.target.value; applyDetails(); updateSwatchUI(); saveState();
  });
  if (els.brandIconColor) {
    els.brandIconColor.addEventListener('input', (e) => {
      state.details.brandIconColor = e.target.value; applyDetails(); saveState();
    });
  }
  els.cardBgColor.addEventListener('input', (e) => {
    state.details.cardBgColor = e.target.value; applyDetails(); saveState();
  });
  els.cardStyle.addEventListener('change', (e) => {
    state.details.cardStyle = e.target.value; applyDetails(); saveState();
  });
  els.btnStyle.addEventListener('change', (e) => {
    state.details.btnStyle = e.target.value; applyDetails(); saveState();
  });
  if (els.btnColor) {
    els.btnColor.addEventListener('input', (e) => {
      state.details.btnColor = e.target.value;
      applyDetails(); updateSwatchUI(); saveState();
    });
  }
  els.fontSize.addEventListener('input', (e) => {
    state.details.fontSize = parseInt(e.target.value, 10); applyDetails(); saveState();
  });
  els.radius.addEventListener('input', (e) => {
    state.details.radius = parseInt(e.target.value, 10); applyDetails(); saveState();
  });
  els.shadow.addEventListener('input', (e) => {
    state.details.shadow = parseInt(e.target.value, 10); applyDetails(); saveState();
  });
  els.textHeading.addEventListener('input', (e) => {
    state.details.textHeading = e.target.value; applyDetails(); saveState();
  });
  els.textBody.addEventListener('input', (e) => {
    state.details.textBody = e.target.value; applyDetails(); saveState();
  });
  els.textSub.addEventListener('input', (e) => {
    state.details.textSub = e.target.value; applyDetails(); saveState();
  });
  els.textBtn.addEventListener('input', (e) => {
    state.details.textBtn = e.target.value; applyDetails(); saveState();
  });

  els.saveBtn.addEventListener('click', () => {
    saveState();
    els.saveBtn.textContent = '✅ 저장됨';
    setTimeout(() => { els.saveBtn.textContent = '💾 저장'; }, 1200);
  });

  els.resetBtn.addEventListener('click', () => {
    if (!confirm('모든 설정을 기본값으로 되돌릴까요?')) return;
    state = deepClone(DEFAULT_STATE);
    saveState(); applyState(); applyMode();
  });

  const modeToggle = $('#modeToggle');
  if (modeToggle) {
    modeToggle.addEventListener('click', (e) => {
      const btn = e.target.closest('.mode-btn');
      if (!btn) return;
      state.mode = btn.dataset.mode;
      applyMode(); saveState();
    });
  }

  // ── 10. 초기 부트 ────────────────────────────────────────
  renderHeatmap();
  applyState();
  applyMode();

  window.__studyRoom = { get state() { return state; }, save: saveState };
})();
