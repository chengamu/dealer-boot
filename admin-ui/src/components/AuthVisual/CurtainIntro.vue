<template>
  <div v-if="visible" ref="introRef" class="curtain-intro" :class="{ leaving }" aria-hidden="true" @click="finish">
    <div class="light-field" />
    <div class="edge-vignette" />
    <div class="shade-rail" />
    <div class="rail-shadow" />
    <div class="shade-lift">
      <span v-for="band in 12" :key="band" class="shade-band" />
    </div>
    <div class="measure-scale">
      <i v-for="tick in 9" :key="tick" />
    </div>
    <div class="light-sweep" />
    <div class="brand-focus">
      <img :src="logo" alt="" />
      <strong>SKYSPF</strong>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import logo from '@/assets/logo/skyspf-mark-d1.svg'

const STORAGE_KEY = 'skyspf:light-control-intro-seen'
const REMOVE_DELAY = 220

const visible = ref(false)
const leaving = ref(false)
const introRef = ref<HTMLElement>()
let animationContext: ReturnType<typeof gsap.context> | undefined
let introTimeline: ReturnType<typeof gsap.timeline> | undefined
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
  introTimeline?.kill()
  leaving.value = true
  removeTimer = window.setTimeout(() => {
    visible.value = false
  }, REMOVE_DELAY)
}

function playIntro() {
  if (!introRef.value) return
  animationContext = gsap.context(() => {
    introTimeline = gsap.timeline({ defaults: { ease: 'power3.out' }, onComplete: finish })
    introTimeline
      .set('.curtain-intro', { autoAlpha: 1 })
      .set('.shade-lift', { autoAlpha: 1, y: 8, rotateX: -4, transformOrigin: '50% 0%' })
      .set('.shade-band', { autoAlpha: 1, y: 8, scaleY: 1, rotateX: -5, transformOrigin: '50% 0%' })
      .set('.shade-rail, .rail-shadow', { autoAlpha: 0, y: -18 })
      .set('.measure-scale i', { autoAlpha: 0, scaleX: 0.4, transformOrigin: '0% 50%' })
      .set('.light-field', { autoAlpha: 0, scale: 1.04 })
      .set('.edge-vignette', { autoAlpha: 0 })
      .set('.light-sweep', { autoAlpha: 0, xPercent: -12 })
      .set('.brand-focus', { autoAlpha: 0, y: 18, scale: 0.94 })
      .to('.light-field', { autoAlpha: 1, scale: 1, duration: 0.5 }, 0)
      .to('.edge-vignette', { autoAlpha: 1, duration: 0.42 }, 0)
      .to('.shade-rail, .rail-shadow', { autoAlpha: 1, y: 0, duration: 0.38 }, 0.02)
      .to('.shade-lift', { y: 0, rotateX: 0, duration: 0.44 }, 0.06)
      .to('.shade-band', { y: 0, rotateX: 0, duration: 0.46, stagger: { each: 0.015, from: 'start' } }, 0.08)
      .to('.measure-scale i', { autoAlpha: 0.72, scaleX: 1, duration: 0.32, stagger: 0.025 }, 0.14)
      .to('.brand-focus', { autoAlpha: 1, y: 0, scale: 1, duration: 0.42 }, 0.2)
      .to('.light-sweep', { autoAlpha: 0.98, xPercent: 430, duration: 1.04, ease: 'power2.inOut' }, 0.22)
      .to('.shade-band', { y: -54, scaleY: 0.08, rotateX: 10, autoAlpha: 0, duration: 0.72, stagger: { each: 0.032, from: 'start' }, ease: 'power3.inOut' }, 0.36)
      .to('.measure-scale', { autoAlpha: 0, x: -10, duration: 0.28, ease: 'power2.in' }, 0.96)
      .to('.shade-rail, .rail-shadow', { autoAlpha: 0, y: -20, duration: 0.24, ease: 'power2.in' }, 1.02)
      .to('.brand-focus', { autoAlpha: 0, y: -16, scale: 1.02, duration: 0.24, ease: 'power2.in' }, 1.08)
      .to('.curtain-intro', { autoAlpha: 0, duration: 0.2, ease: 'power1.out' }, 1.2)
  }, introRef.value)
}

onMounted(async () => {
  if (hasSeenIntro() || prefersReducedMotion()) return
  visible.value = true
  await nextTick()
  playIntro()
})

onBeforeUnmount(() => {
  introTimeline?.kill()
  animationContext?.revert()
  window.clearTimeout(removeTimer)
})
</script>

<style scoped lang="scss" src="./CurtainIntro.scss"></style>
