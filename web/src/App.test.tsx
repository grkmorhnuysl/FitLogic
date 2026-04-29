import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App counter', () => {
  it('increments count when clicking the button', () => {
    render(<App />)

    const button = screen.getByRole('button', { name: /count is 0/i })
    fireEvent.click(button)

    expect(screen.getByRole('button', { name: /count is 1/i })).toBeInTheDocument()
  })
})
