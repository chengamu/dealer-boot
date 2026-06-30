const styleId = 'bocoo-page-agent-panel-enhancer-style'

export function installPanelEnhancerStyle() {
  if (document.getElementById(styleId)) return
  const style = document.createElement('style')
  style.id = styleId
  style.textContent = `
    #page-agent-runtime_agent-panel .bocoo-agent-md {
      display: grid;
      gap: 6px;
      color: inherit;
      line-height: 1.7;
      white-space: normal;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-md p,
    #page-agent-runtime_agent-panel .bocoo-agent-md ul,
    #page-agent-runtime_agent-panel .bocoo-agent-md ol,
    #page-agent-runtime_agent-panel .bocoo-agent-md h3,
    #page-agent-runtime_agent-panel .bocoo-agent-md h4,
    #page-agent-runtime_agent-panel .bocoo-agent-md h5 {
      margin: 0;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-md ul,
    #page-agent-runtime_agent-panel .bocoo-agent-md ol {
      padding-left: 18px;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-md li + li {
      margin-top: 3px;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-md code {
      padding: 1px 5px;
      border-radius: 5px;
      background: rgba(36, 91, 166, 0.08);
      color: #1d4f8f;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      font-size: 0.92em;
    }

    #page-agent-runtime_agent-panel [data-bocoo-typewriter="running"]::after {
      content: '';
      display: inline-block;
      width: 6px;
      height: 1em;
      margin-left: 2px;
      vertical-align: -2px;
      border-right: 2px solid #1d6cff;
      animation: bocoo-agent-caret 0.8s steps(1) infinite;
    }

    @keyframes bocoo-agent-caret {
      50% { opacity: 0; }
    }

    #page-agent-runtime_agent-panel .bocoo-agent-live-card {
      display: flex;
      gap: 10px;
      align-items: flex-start;
      width: fit-content;
      max-width: min(360px, 84%);
      margin-top: 12px;
      padding: 10px 12px;
      color: #213a63;
      background: #ffffff;
      border-radius: 14px 14px 14px 7px;
      box-shadow: 0 8px 20px rgba(28, 48, 84, 0.08);
      line-height: 1.55;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running {
      width: min(420px, calc(100vw - 32px)) !important;
      height: 218px !important;
      bottom: 28px !important;
      padding: 14px !important;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running [class*="historySectionWrapper"] {
      height: calc(100% - 52px) !important;
      background: rgba(255, 255, 255, 0.92) !important;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running [class*="historySection"] {
      padding: 10px !important;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running [class*="inputSectionWrapper"] {
      display: none !important;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running [class*="historyItem"] {
      max-width: 100% !important;
      margin-top: 8px !important;
      padding: 8px 10px !important;
      font-size: 13px !important;
      line-height: 1.45 !important;
    }

    #page-agent-runtime_agent-panel.bocoo-agent-running .bocoo-agent-live-card {
      max-width: 100%;
      margin-top: 8px;
      padding: 8px 10px;
      font-size: 13px;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-live-icon {
      width: 8px;
      height: 8px;
      flex: none;
      margin-top: 8px;
      border-radius: 50%;
      background: #1d6cff;
      box-shadow: 0 0 0 5px rgba(29, 108, 255, 0.12);
      animation: bocoo-agent-pulse 1s ease-in-out infinite;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-live-text {
      min-width: 0;
      white-space: normal;
      word-break: break-word;
    }

    #page-agent-runtime_agent-panel .bocoo-agent-live-card.is-typing .bocoo-agent-live-text::after {
      content: '';
      display: inline-block;
      width: 6px;
      height: 1em;
      margin-left: 2px;
      vertical-align: -2px;
      border-right: 2px solid #1d6cff;
      animation: bocoo-agent-caret 0.8s steps(1) infinite;
    }

    @keyframes bocoo-agent-pulse {
      50% {
        transform: scale(0.72);
        opacity: 0.55;
      }
    }

    #page-agent-runtime_simulator-mask {
      cursor: none !important;
    }

    #page-agent-runtime_simulator-mask > *:first-child {
      opacity: 0.12 !important;
      filter: hue-rotate(185deg) saturate(0.55) brightness(1.12) !important;
    }

    #page-agent-runtime_simulator-mask [class*="cursor"] {
      display: block !important;
    }

    #page-agent-runtime_simulator-mask [class*="cursorBorder"] {
      background: linear-gradient(45deg, #0b6dff, #2aa7ff) !important;
      filter:
        drop-shadow(0 0 5px rgba(11, 109, 255, 0.58))
        drop-shadow(0 0 12px rgba(42, 167, 255, 0.28)) !important;
    }

    #page-agent-runtime_simulator-mask [class*="cursorFilling"] {
      filter:
        drop-shadow(0 1px 3px rgba(0, 0, 0, 0.28))
        drop-shadow(0 0 8px rgba(11, 109, 255, 0.36)) !important;
    }

    #page-agent-runtime_simulator-mask [class*="cursorRipple"]::after {
      border-color: rgba(11, 109, 255, 0.95) !important;
      box-shadow:
        0 0 0 4px rgba(11, 109, 255, 0.12),
        0 0 18px rgba(42, 167, 255, 0.22) !important;
    }
  `
  document.head.appendChild(style)
}
