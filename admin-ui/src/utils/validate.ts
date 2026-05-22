export function isHttp(url: string) {
  return url.includes('http://') || url.includes('https://')
}

export function isExternal(path: string) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

export function validUsername(str: string) {
  const validMap = ['admin', 'editor']
  return validMap.includes(str.trim())
}

export function validURL(url: string) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

export function validLowerCase(str: string) {
  return /^[a-z]+$/.test(str)
}

export function validUpperCase(str: string) {
  return /^[A-Z]+$/.test(str)
}

export function validAlphabets(str: string) {
  return /^[A-Za-z]+$/.test(str)
}

export function validEmail(email: string) {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

export function isString(str: unknown) {
  return typeof str === 'string' || str instanceof String
}

export function isArray(arg: unknown) {
  return Array.isArray(arg)
}
