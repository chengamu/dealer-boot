export type LoginLegalType = 'privacy' | 'terms' | 'cookie'

export interface LoginFormModel {
  username: string
  password: string
  code: string
  uuid: string
}

export interface CaptchaState {
  enabled: boolean
  img: string
}
