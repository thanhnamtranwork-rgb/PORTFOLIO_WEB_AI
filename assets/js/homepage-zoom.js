/*
 * Desktop-only visual-size compensation for the Portfolio landing screen.
 *
 * The reference composition is the normal Chrome desktop view at 125% zoom.
 * Chromium changes devicePixelRatio when browser zoom changes; CSS zoom is
 * compensated by TARGET_VISUAL_SCALE / devicePixelRatio so the page remains
 * the same physical size at 100%, 80%, 50% and 25% browser zoom.
 *
 * The check uses the effective physical viewport width, so a desktop browser
 * zoomed far out is still treated as desktop even though innerWidth is large.
 * Touch/mobile layouts are left to the normal responsive breakpoints.
 */
(function () {
  'use strict';

  const root = document.documentElement;
  const TARGET_VISUAL_SCALE = 1.25;
  const MIN_COMPENSATION = 0.5;
  const MAX_COMPENSATION = 5;
  let resizeFrame = 0;

  function isDesktopPointer() {
    return window.matchMedia('(hover: hover) and (pointer: fine)').matches;
  }

  function updateHomepageZoom() {
    const dpr = Number(window.devicePixelRatio) || 1;
    const physicalViewportWidth = window.innerWidth * dpr;
    const useCompensation = isDesktopPointer() && physicalViewportWidth >= 1025;

    if (!useCompensation) {
      root.removeAttribute('data-homepage-zoom-fixed');
      root.style.removeProperty('--homepage-zoom-compensation');
      return;
    }

    const compensation = Math.min(
      MAX_COMPENSATION,
      Math.max(MIN_COMPENSATION, TARGET_VISUAL_SCALE / dpr)
    );

    root.style.setProperty('--homepage-zoom-compensation', compensation.toFixed(4));
    root.setAttribute('data-homepage-zoom-fixed', 'true');
  }

  function scheduleUpdate() {
    if (resizeFrame) window.cancelAnimationFrame(resizeFrame);
    resizeFrame = window.requestAnimationFrame(updateHomepageZoom);
  }

  updateHomepageZoom();
  window.addEventListener('resize', scheduleUpdate, { passive: true });
  window.addEventListener('orientationchange', scheduleUpdate, { passive: true });
})();
