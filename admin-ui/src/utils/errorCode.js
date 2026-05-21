import { getMessage } from '@/locales'
import { getLocale } from '@/utils/auth'

export default new Proxy({}, {
  get(_, code) {
    return getMessage(`errorCode.${String(code)}`, getLocale()) || getMessage('errorCode.default', getLocale())
  }
})
