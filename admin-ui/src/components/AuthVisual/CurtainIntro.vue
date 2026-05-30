<template>
  <div v-if="visible" class="curtain-intro" :class="{ leaving }" aria-hidden="true" @click="finish">
    <div class="light-slit" />
    <div class="curtain-panel curtain-left" />
    <div class="curtain-panel curtain-right" />
    <div class="curtain-logo">skyspf</div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const STORAGE_KEY = 'skyspf:curtain-intro-seen'
const INTRO_DURATION = 1350
const REMOVE_DELAY = 180

const visible = ref(false)
const leaving = ref(false)
let finishTimer: number | undefined
let removeTimer: number | undefined

function markSeen() {
  try {
    window.sessionStorage.setItem(STORAGE_KEY, '1')
  } catch {
    // Ignore storage failures; the animation still removes itself.
  }
}

function hasSeenIntro() {
  try {
    return window.sessionStorage.getItem(STORAGE_KEY) === '1'
  } catch {
    return false
  }
}

function prefersReducedMotion() {
  return window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
}

function finish() {
  if (!visible.value) return
  markSeen()
  window.clearTimeout(finishTimer)
  leaving.value = true
  removeTimer = window.setTimeout(() => {
    visible.value = false
  }, REMOVE_DELAY)
}

onMounted(() => {
  if (hasSeenIntro() || prefersReducedMotion()) return
  visible.value = true
  finishTimer = window.setTimeout(finish, INTRO_DURATION)
})

onBeforeUnmount(() => {
  window.clearTimeout(finishTimer)
  window.clearTimeout(removeTimer)
})
</script>

<style scoped lang="scss">
.curtain-intro {
  position: fixed;
  inset: 0;
  z-index: 3000;
  overflow: hidden;
  cursor: pointer;
  background: radial-gradient(circle at 50% 48%, #ffffff 0%, #eef6ff 58%, #c8ddf8 100%);
  perspective: 1200px;
  perspective-origin: 50% 30%;
}

.curtain-panel {
  position: absolute;
  top: -10%;
  width: 50.5%;
  height: 120%;
  pointer-events: none;
  background-blend-mode: multiply;
  will-change: transform, opacity;
  animation-duration: 1.35s;
  animation-timing-function: cubic-bezier(0.25, 1, 0.4, 1);
  animation-fill-mode: forwards;
}

.curtain-left {
  left: 0;
  transform-origin: left top;
  animation-name: open-left;
  background:
    repeating-linear-gradient(0deg, rgba(0, 0, 0, 0.018) 0 1px, transparent 1px 3px),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.1) 0 1px, transparent 1px 4px),
    repeating-linear-gradient(
      90deg,
      #ffffff 0%,
      #f2f7fd 10%,
      #d6e7f8 22%,
      #a7cdf5 32%,
      #d6e7f8 42%,
      #f2f7fd 52%,
      #ffffff 60%
    );
  background-size: 100% 100%, 100% 100%, 150px 100%;
  box-shadow: inset -24px 0 42px -15px rgba(7, 27, 87, 0.18), 14px 0 34px rgba(0, 0, 0, 0.05);
}

.curtain-right {
  right: 0;
  transform-origin: right top;
  animation-name: open-right;
  background:
    repeating-linear-gradient(0deg, rgba(0, 0, 0, 0.018) 0 1px, transparent 1px 3px),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.1) 0 1px, transparent 1px 4px),
    repeating-linear-gradient(
      -90deg,
      #ffffff 0%,
      #f2f7fd 10%,
      #d6e7f8 22%,
      #a7cdf5 32%,
      #d6e7f8 42%,
      #f2f7fd 52%,
      #ffffff 60%
    );
  background-size: 100% 100%, 100% 100%, 150px 100%;
  box-shadow: inset 24px 0 42px -15px rgba(7, 27, 87, 0.18), -14px 0 34px rgba(0, 0, 0, 0.05);
}

.light-slit {
  position: absolute;
  top: 0;
  left: 50%;
  z-index: 2;
  width: 4px;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.95), transparent);
  box-shadow:
    0 0 80px 28px rgba(255, 255, 255, 0.82),
    0 0 160px 64px rgba(193, 221, 255, 0.44);
  transform: translateX(-50%);
  animation: light-open 1.35s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}

.curtain-logo {
  position: absolute;
  top: 45%;
  left: 50%;
  z-index: 3;
  color: #071b57;
  font-size: clamp(40px, 6vw, 76px);
  font-weight: 300;
  letter-spacing: 0.18em;
  pointer-events: none;
  text-shadow: 0 10px 30px rgba(7, 27, 87, 0.12);
  transform: translate(-50%, -50%);
  animation: logo-fade 1.35s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}

.curtain-intro.leaving {
  pointer-events: none;
  animation: overlay-gone 0.18s ease forwards;
}

@keyframes open-left {
  0% {
    opacity: 1;
    transform: rotateY(0deg) rotateZ(0deg) scaleX(1) translateX(0);
  }

  24% {
    opacity: 1;
    transform: rotateY(-12deg) rotateZ(-1.6deg) scaleX(0.96) translateX(-1%);
  }

  58% {
    opacity: 0.95;
    transform: rotateY(-46deg) rotateZ(-8deg) scaleX(0.42) translateX(-35%);
  }

  100% {
    opacity: 0;
    transform: rotateY(-76deg) rotateZ(-17deg) scaleX(0.02) translateX(-130%);
  }
}

@keyframes open-right {
  0% {
    opacity: 1;
    transform: rotateY(0deg) rotateZ(0deg) scaleX(1) translateX(0);
  }

  24% {
    opacity: 1;
    transform: rotateY(12deg) rotateZ(1.6deg) scaleX(0.96) translateX(1%);
  }

  58% {
    opacity: 0.95;
    transform: rotateY(46deg) rotateZ(8deg) scaleX(0.42) translateX(35%);
  }

  100% {
    opacity: 0;
    transform: rotateY(76deg) rotateZ(17deg) scaleX(0.02) translateX(130%);
  }
}

@keyframes light-open {
  0% {
    width: 4px;
    opacity: 0;
  }

  18% {
    width: 18px;
    opacity: 0.95;
  }

  50% {
    width: 42vw;
    opacity: 1;
  }

  78% {
    width: 100vw;
    opacity: 0.38;
  }

  100% {
    width: 100vw;
    opacity: 0;
  }
}

@keyframes logo-fade {
  0% {
    opacity: 0;
    letter-spacing: 0.12em;
    transform: translate(-50%, -50%) scale(0.96);
  }

  24% {
    opacity: 1;
    letter-spacing: 0.18em;
    transform: translate(-50%, -50%) scale(1);
  }

  62% {
    opacity: 0.82;
    filter: blur(0);
  }

  100% {
    opacity: 0;
    letter-spacing: 0.22em;
    filter: blur(6px);
    transform: translate(-50%, -50%) scale(1.08);
  }
}

@keyframes overlay-gone {
  to {
    opacity: 0;
    visibility: hidden;
  }
}

@media (prefers-reduced-motion: reduce) {
  .curtain-intro {
    display: none;
  }
}
</style>
