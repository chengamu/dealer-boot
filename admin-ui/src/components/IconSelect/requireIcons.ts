const icons: string[] = []

const modules = import.meta.glob('./../../assets/icons/svg/*.svg')

for (const path in modules) {
  icons.push(path.split('assets/icons/svg/')[1].split('.svg')[0])
}

export default icons
