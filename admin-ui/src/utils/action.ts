export async function runUiAction<T>(action: () => Promise<T> | T, onError?: (error: unknown) => void): Promise<T | undefined> {
  try {
    return await action()
  } catch (error) {
    onError?.(error)
    return undefined
  }
}
