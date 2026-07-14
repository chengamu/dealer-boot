import assert from 'node:assert/strict'
import test from 'node:test'
// @ts-expect-error Node's type-stripping test runner loads the TypeScript source directly.
import { resolvePayPageMode } from './payPageModes.ts'

function route(path: string, title: string) {
  return { path, name: '', meta: { title } } as never
}

test('merchant credit route uses business credit mode', () => {
  assert.equal(
    resolvePayPageMode(route('/finance/creditAccounts', 'finance.business.creditAccounts'), ['pay:credit:query']),
    'business-credit'
  )
})

test('merchant receivable route uses business receivable mode', () => {
  assert.equal(
    resolvePayPageMode(route('/finance/receivables', 'finance.business.receivables'), ['pay:receivable:list']),
    'business-receivable'
  )
})
