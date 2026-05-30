export {}

declare global {
  type GoogleCredentialResponse = {
    credential?: string
  }

  type GoogleAccountsId = {
    initialize: (config: { client_id: string; callback: (response: GoogleCredentialResponse) => void }) => void
    renderButton: (parent: HTMLElement, options: Record<string, string | number | boolean>) => void
  }

  interface Window {
    google?: {
      accounts?: {
        id?: GoogleAccountsId
      }
    }
  }
}
