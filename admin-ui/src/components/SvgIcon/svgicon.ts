import type { App, Component } from 'vue'
import * as components from '@element-plus/icons-vue'

export default {
  install(app: App) {
    Object.values(components).forEach((component) => {
      const icon = component as Component & { name?: string }
      if (icon.name) {
        app.component(icon.name, icon)
      }
    })
  }
}
