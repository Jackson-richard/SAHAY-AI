---
name: Public Infrastructure Design System
colors:
  surface: '#f8f9fa'
  surface-dim: '#d9dadb'
  surface-bright: '#f8f9fa'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f4f5'
  surface-container: '#edeeef'
  surface-container-high: '#e7e8e9'
  surface-container-highest: '#e1e3e4'
  on-surface: '#191c1d'
  on-surface-variant: '#3c4947'
  inverse-surface: '#2e3132'
  inverse-on-surface: '#f0f1f2'
  outline: '#6c7a77'
  outline-variant: '#bbcac6'
  surface-tint: '#006b5f'
  primary: '#006b5f'
  on-primary: '#ffffff'
  primary-container: '#14b8a6'
  on-primary-container: '#00423b'
  inverse-primary: '#4fdbc8'
  secondary: '#515f74'
  on-secondary: '#ffffff'
  secondary-container: '#d5e3fc'
  on-secondary-container: '#57657a'
  tertiary: '#006a61'
  on-tertiary: '#ffffff'
  tertiary-container: '#44b5a8'
  on-tertiary-container: '#00423c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#71f8e4'
  primary-fixed-dim: '#4fdbc8'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005048'
  secondary-fixed: '#d5e3fc'
  secondary-fixed-dim: '#b9c7df'
  on-secondary-fixed: '#0d1c2e'
  on-secondary-fixed-variant: '#3a485b'
  tertiary-fixed: '#89f5e7'
  tertiary-fixed-dim: '#6bd8cb'
  on-tertiary-fixed: '#00201d'
  on-tertiary-fixed-variant: '#005049'
  background: '#f8f9fa'
  on-background: '#191c1d'
  surface-variant: '#e1e3e4'
typography:
  display-lg:
    fontFamily: Public Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Public Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Public Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  touch-target: 48px
  container-margin: 16px
---

## Brand & Style

The visual identity of this design system is built upon the concept of "Trusted Digital Public Infrastructure." It moves away from the ephemeral aesthetics of typical technology startups and instead embraces a grounded, institutional character. The goal is to evoke a sense of stability, accessibility, and official reliability, functioning as a digital utility for citizens rather than a private product.

The design style is **Corporate / Modern**, prioritizing absolute clarity over visual flourish. By utilizing heavy whitespace and a strictly functional layout, the system creates a calming environment for users monitoring their wellbeing. It avoids all futuristic AI tropes, such as gradients or glowing effects, in favor of solid surfaces and legible, high-contrast typography that feels authoritative yet approachable across diverse demographic groups.

## Colors

The color palette is centered on a calm, professional Teal (`#14B8A6`), chosen for its association with health and official services without being overtly clinical. 

- **Primary:** Used for main actions, active states, and brand recognition.
- **Secondary (Slate):** Used for secondary information, icons, and non-critical UI elements to maintain a neutral tone.
- **Background:** A very light gray (`#F9FAFB`) is used to reduce screen glare and provide a soft canvas for pure white cards.
- **Semantic Red:** Reserved strictly for genuine urgency or critical health warnings. 
- **Contrast:** All color combinations must meet or exceed WCAG AA standards to ensure readability for users with varying visual abilities.

## Typography

This design system uses a dual-font approach to balance institutional authority with technical legibility. **Public Sans** is used for headlines to provide a clean, official feel, while **Inter** is used for body copy and labels to ensure maximum clarity on mobile displays.

The typography is optimized for multilingual support, including Hindi, Tamil, Malayalam, and Telugu. When rendering native scripts:
- Maintain the same line-height ratios to prevent vertical clipping.
- Ensure the font weight is slightly heavier for Indic scripts to maintain visual parity with Latin text.
- Standard body text should never drop below 16px to ensure accessibility for elderly users or those in low-light environments.

## Layout & Spacing

The system follows a **fluid grid model** optimized for Android devices. A 4-column grid is used for mobile portrait views, with a standard 16px margin on either side of the screen.

- **Rhythm:** An 8px linear scale is used for all layout adjustments, with a 4px increment available for tight component spacing.
- **Touch Targets:** All interactive elements (buttons, toggles, links) must have a minimum hit area of 48x48px, even if the visual element is smaller.
- **White Space:** Generous vertical padding (24px - 32px) is used between major sections to prevent cognitive overload and maintain a "calm" interface.

## Elevation & Depth

To maintain the "Trusted Infrastructure" aesthetic, the system avoids aggressive shadows and complex depth. Hierarchy is established through:

- **Tonal Layers:** The primary background is `#F9FAFB`. Elevated elements, such as content cards, are pure `#FFFFFF`.
- **Subtle Shadows:** When necessary, use extremely soft, diffused shadows (Blur: 12px, Y: 4px, Opacity: 4% Black) to distinguish interactive cards from the background.
- **Flat Outlines:** For input fields and secondary buttons, use a 1px solid border in Slate-200 or Slate-300 rather than a shadow. This creates a more disciplined, official look.

## Shapes

The shape language is defined by a friendly but structured "Rounded" philosophy. This softens the institutional feel without appearing overly "bubbly" or playful.

- **Standard Components:** Buttons and input fields use a **8px (0.5rem)** corner radius.
- **Containers:** Large cards and informational modules use a **16px (1rem)** corner radius to create a distinct visual container for data.
- **Selection Indicators:** Checkboxes use a small 4px radius, while radio buttons remain circular to follow platform conventions.

## Components

### Buttons
- **Primary:** Solid Teal (`#14B8A6`) with white text. High-contrast and easily identifiable.
- **Secondary:** White background with a Slate-300 border and Slate-700 text.
- **Tertiary:** Text-only with an underline or bold weight, used for less frequent actions.

### Cards
Cards are the primary way information is surfaced. They must be pure white, have a 16px corner radius, and use 16px of internal padding. Group related data points (e.g., daily steps, heart rate trends) within separate cards to maintain clean separation.

### Input Fields
Fields must include a visible, persistent label above the input area. The target area must be at least 48px high. Use a 1px border that thickens to 2px in the primary Teal color when focused.

### Chips & Tags
Used for filtering or status indicators. These use a Pill-shape (fully rounded) and a background of Slate-100 to remain distinct from primary action buttons.

### List Items
List items should have a minimum height of 56px to ensure easy tapping. They should be separated by a 1px horizontal divider (Slate-100) or 8px of vertical spacing.

### Accessibility Notes
- Icons must be accompanied by text labels whenever possible.
- Avoid abstract iconography; use literal, recognizable symbols that are culturally neutral.