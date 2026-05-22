import { getMessage } from '@/locales'
import { getLocale } from '@/utils/auth'

const errorCode = new Proxy<Record<string, string>>(
  {},
  {
    get(_, code) {
      return getMessage(`errorCode.${String(code)}`, getLocale()) || getMessage('errorCode.default', getLocale())
    }
  }
)

export default errorCode
