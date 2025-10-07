# Kotlio Theme System

The Kotlio web interface uses a modern, CSS variable-based theme system that automatically adapts to light and dark modes.

## Features

✨ **Modern Design System**
- Clean, professional aesthetic
- Purple accent colors (violet/indigo palette)
- Smooth transitions and animations
- Elevated cards with modern shadows

🌓 **Automatic Dark Mode**
- Respects user's system preference (`prefers-color-scheme`)
- Seamless transitions between modes
- Optimized contrast for readability

🎨 **CSS Variables for Everything**
- Colors, spacing, typography, shadows
- Easy to customize and extend
- Consistent design tokens throughout

📱 **Fully Responsive**
- Mobile-first approach
- Breakpoints at 768px
- Touch-friendly interactive elements

## Theme Tokens

### Color Palette

#### Light Mode
```css
--color-bg-primary: #ffffff      /* Main backgrounds */
--color-bg-secondary: #f8f9fa    /* Page background */
--color-bg-tertiary: #e9ecef     /* Code blocks, subtle backgrounds */
--color-text-primary: #1a1a1a    /* Headings, labels */
--color-text-secondary: #4a5568  /* Body text */
--color-text-tertiary: #718096   /* Muted text */
```

#### Dark Mode
```css
--color-bg-primary: #0f172a      /* Slate 900 */
--color-bg-secondary: #1e293b    /* Slate 800 */
--color-bg-tertiary: #334155     /* Slate 700 */
--color-text-primary: #f1f5f9    /* Slate 100 */
--color-text-secondary: #cbd5e1  /* Slate 300 */
--color-text-tertiary: #94a3b8   /* Slate 400 */
```

### Brand Colors

```css
--color-primary: #7c3aed         /* Violet 600 (light) / #8b5cf6 (dark) */
--color-success: #10b981         /* Emerald 500 / #34d399 (dark) */
--color-warning: #f59e0b         /* Amber 500 / #fbbf24 (dark) */
--color-error: #ef4444           /* Red 500 / #f87171 (dark) */
```

### Spacing Scale

```css
--space-xs: 0.25rem    /* 4px */
--space-sm: 0.5rem     /* 8px */
--space-md: 1rem       /* 16px */
--space-lg: 1.5rem     /* 24px */
--space-xl: 2rem       /* 32px */
--space-2xl: 3rem      /* 48px */
```

### Border Radius

```css
--radius-sm: 0.375rem  /* 6px - Small elements */
--radius-md: 0.5rem    /* 8px - Buttons, inputs */
--radius-lg: 0.75rem   /* 12px - Code blocks */
--radius-xl: 1rem      /* 16px - Cards, sections */
```

### Typography

```css
--font-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto'...
--font-mono: 'SF Mono', 'Monaco', 'Cascadia Code', 'Roboto Mono'...

--font-size-sm: 0.875rem    /* 14px */
--font-size-base: 1rem      /* 16px */
--font-size-lg: 1.125rem    /* 18px */
--font-size-xl: 1.25rem     /* 20px */
--font-size-2xl: 1.5rem     /* 24px */
--font-size-3xl: 1.875rem   /* 30px */

--line-height-tight: 1.25
--line-height-normal: 1.5
--line-height-relaxed: 1.75
```

### Shadows

```css
--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05)
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1)...
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1)...
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1)...
```

### Transitions

```css
--transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1)
--transition-base: 200ms cubic-bezier(0.4, 0, 0.2, 1)
--transition-slow: 300ms cubic-bezier(0.4, 0, 0.2, 1)
```

## Component States

### Buttons

```css
/* Base state */
background: var(--color-primary)
box-shadow: var(--shadow-sm)

/* Hover */
background: var(--color-primary-hover)
box-shadow: var(--shadow-md)
transform: translateY(-1px)

/* Active */
transform: translateY(0)
box-shadow: var(--shadow-sm)

/* Focus */
outline: 2px solid var(--color-primary)
outline-offset: 2px
```

### Inputs

```css
/* Base state */
border: 1px solid var(--color-border)

/* Hover */
border-color: var(--color-border-hover)

/* Focus */
border-color: var(--color-primary)
box-shadow: 0 0 0 3px var(--color-primary-light)
```

### Output Panels

```css
/* Default */
border-left: 4px solid var(--color-primary)
background: var(--color-bg-secondary)

/* Success */
.output.success {
    border-left-color: var(--color-success)
    background: var(--color-success-light)
}

/* Error */
.output.error {
    border-left-color: var(--color-error)
    background: var(--color-error-light)
}

/* Warning */
.output.warning {
    border-left-color: var(--color-warning)
    background: var(--color-warning-light)
}
```

## Customization

### Changing the Brand Color

To change from purple to a different brand color, update the `:root` and dark mode variables:

```css
:root {
    /* Change to blue */
    --color-primary: #3b82f6;
    --color-primary-hover: #2563eb;
    --color-primary-light: #dbeafe;
}

@media (prefers-color-scheme: dark) {
    :root {
        --color-primary: #60a5fa;
        --color-primary-hover: #93c5fd;
        --color-primary-light: #1e3a8a;
    }
}
```

### Adding a New Color Variant

```css
:root {
    --color-info: #0ea5e9;
    --color-info-light: #e0f2fe;
}

@media (prefers-color-scheme: dark) {
    :root {
        --color-info: #38bdf8;
        --color-info-light: #075985;
    }
}
```

Then use it in components:

```css
.output.info {
    border-left-color: var(--color-info);
    background: var(--color-info-light);
}
```

### Customizing Spacing

```css
:root {
    /* Tighter spacing */
    --space-md: 0.75rem;
    --space-lg: 1.25rem;
    
    /* Or looser spacing */
    --space-md: 1.25rem;
    --space-lg: 2rem;
}
```

## Animations

### Fade In

All `.demo-section` elements animate in with a subtle fade and slide:

```css
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.demo-section {
    animation: fadeIn var(--transition-slow) ease-out;
}
```

### Hover Effects

Buttons and cards have smooth hover effects:

```css
button:hover {
    transform: translateY(-1px);  /* Lift effect */
    box-shadow: var(--shadow-md);
}

.demo-section:hover {
    box-shadow: var(--shadow-xl);  /* Enhanced shadow */
}
```

## Accessibility

### Focus States

All interactive elements have visible focus indicators:

```css
button:focus {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
}

input:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px var(--color-primary-light);
}
```

### Color Contrast

All text colors meet WCAG AA standards:
- Primary text: 7:1 contrast ratio
- Secondary text: 4.5:1 contrast ratio
- Interactive elements: Clear visual feedback

### Reduced Motion

To respect user preferences for reduced motion:

```css
@media (prefers-reduced-motion: reduce) {
    * {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
    }
}
```

## Mobile Responsive Breakpoints

### 768px and Below

```css
@media (max-width: 768px) {
    body {
        padding: var(--space-md);  /* Tighter padding */
    }
    
    h1 {
        font-size: var(--font-size-2xl);  /* Smaller headings */
    }
    
    input[type="text"] {
        max-width: 100%;  /* Full width inputs */
    }
}
```

## Best Practices

### ✅ DO

- Use CSS variables for all values
- Maintain consistent spacing scales
- Test in both light and dark modes
- Provide focus indicators for all interactive elements
- Use semantic HTML

### ❌ DON'T

- Hardcode color values
- Use inline styles (except for dynamic values)
- Skip hover/focus states
- Ignore dark mode
- Use fixed pixel values for responsive elements

## Browser Support

The theme system uses modern CSS features:
- CSS Custom Properties (CSS Variables)
- `prefers-color-scheme` media query
- CSS Grid and Flexbox
- Modern scrollbar styling (`::-webkit-scrollbar`)

**Minimum Browser Versions:**
- Chrome/Edge: 88+
- Firefox: 86+
- Safari: 14+
- Opera: 74+

## Performance

### Optimizations

- Single CSS file (no external dependencies)
- Hardware-accelerated transforms
- Efficient selectors
- Minimal repaints with CSS variables
- Lazy animations (only on elements that need them)

### File Size

The complete theme is approximately:
- **Uncompressed:** ~10 KB
- **Gzipped:** ~3 KB

## Future Enhancements

Potential additions:
- [ ] Light/dark mode toggle button
- [ ] Multiple theme presets (blue, green, red)
- [ ] High contrast mode
- [ ] Custom CSS file override support
- [ ] Theme configuration via JSON

## Summary

The Kotlio theme provides a modern, accessible, and maintainable design system using CSS variables. It automatically adapts to user preferences and provides a consistent, professional experience across all devices and color schemes.

🎨 **Modern** • 🌓 **Dark Mode Ready** • 📱 **Responsive** • ♿ **Accessible**
