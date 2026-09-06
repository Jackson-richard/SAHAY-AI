---
name: Public Trust Infrastructure
colors:
  surface: '#f6faf9'
  surface-dim: '#d7dbda'
  surface-bright: '#f6faf9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f4f3'
  surface-container: '#ebefee'
  surface-container-high: '#e5e9e8'
  surface-container-highest: '#dfe3e2'
  on-surface: '#181c1c'
  on-surface-variant: '#3e4949'
  inverse-surface: '#2c3131'
  inverse-on-surface: '#edf2f1'
  outline: '#6e7979'
  outline-variant: '#bdc9c8'
  surface-tint: '#006a6a'
  primary: '#006565'
  on-primary: '#ffffff'
  primary-container: '#008080'
  on-primary-container: '#e3fffe'
  inverse-primary: '#76d6d5'
  secondary: '#3a5f94'
  on-secondary: '#ffffff'
  secondary-container: '#9fc2fe'
  on-secondary-container: '#294f83'
  tertiary: '#585b5c'
  on-tertiary: '#ffffff'
  tertiary-container: '#717374'
  on-tertiary-container: '#f9fafb'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#93f2f2'
  primary-fixed-dim: '#76d6d5'
  on-primary-fixed: '#002020'
  on-primary-fixed-variant: '#004f4f'
  secondary-fixed: '#d5e3ff'
  secondary-fixed-dim: '#a7c8ff'
  on-secondary-fixed: '#001b3c'
  on-secondary-fixed-variant: '#1f477b'
  tertiary-fixed: '#e1e3e4'
  tertiary-fixed-dim: '#c5c7c8'
  on-tertiary-fixed: '#191c1d'
  on-tertiary-fixed-variant: '#454748'
  background: '#f6faf9'
  on-background: '#181c1c'
  surface-variant: '#dfe3e2'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
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
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  touch-target-min: 48px
  margin-mobile: 20px
  gutter-mobile: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style

The brand identity is built on the pillars of stability, transparency, and trauma-informed design. It positions itself as "Digital Public Infrastructure"—a reliable utility rather than a commercial product. The visual language avoids the decorative "magic" of typical AI applications, opting instead for a **Corporate / Modern** aesthetic that emphasizes institutional reliability and calm.

The target audience includes victims of crisis and government social workers who require a high-reliability, low-friction interface. The emotional response must be one of safety and containment; the UI acts as a steady hand through a standard, highly accessible layout that prioritizes clarity over flair.

## Colors

The palette is rooted in a calm Teal (#008080) which serves as the primary action color, chosen for its psychological associations with healing and professional care. Deep Navy (#003366) is utilized for headers and structural elements to provide a sense of grounded authority.

The background uses an Off-White (#F8F9FA) to reduce screen glare and eye strain during prolonged use. Text is set in a high-contrast Dark Grey (#212529) to ensure AAA accessibility. Red is strictly reserved for "Urgent" or "High-Risk" states, ensuring that its appearance immediately signals the need for intervention without causing unnecessary alarm across the rest of the interface.

## Typography

This design system utilizes **Inter** across all levels for its utilitarian clarity and excellent legibility at small sizes. The scale is intentionally generous to accommodate users in high-stress situations who may have difficulty processing dense information.

- **Headlines:** Use Bold weights for immediate orientation.
- **Body Text:** Uses a 18px base for primary reading (body-lg) to ensure high accessibility.
- **Line Heights:** Set wider than standard (1.5x for body) to improve readability and prevent the "wall of text" effect.
- **Alignment:** Always left-aligned for RTL languages to maintain a predictable reading pattern.

## Layout & Spacing

The layout follows a **Fluid Grid** model optimized for mobile-first environments. A 4-column grid is used for mobile devices with a 20px outer margin to provide a "safe zone" for thumb interaction.

Every interactive element adheres to a minimum **48px touch target** rule to accommodate varying levels of motor precision. Vertical spacing (stacking) follows a strict 8px linear scale. For content-heavy monitoring screens, use 24px spacing between cards to create clear visual separation and reduce cognitive load.

## Elevation & Depth

This design system employs **Tonal Layers** supplemented by very soft, ambient shadows. Depth is used functionally rather than decoratively—to separate the background from interactive content.

- **Level 0 (Background):** Off-white (#F8F9FA), flat.
- **Level 1 (Cards/Containers):** Pure white (#FFFFFF) with a 2px blur, 5% opacity black shadow. This creates a subtle "lift" that distinguishes a piece of information from the canvas.
- **Level 2 (Active/Floating):** Used for bottom navigation bars or primary action buttons, featuring a slightly more pronounced shadow (8px blur, 8% opacity) to indicate they sit above the content flow.

## Shapes

The shape language is defined by **Rounded** geometry (Level 2). This specific radius of 16px (1rem) for primary cards and 8px (0.5rem) for smaller components like buttons balances professional structure with an approachable, non-threatening softness. 

- **Primary Cards:** 16px radius.
- **Buttons/Inputs:** 8px radius.
- **Selection Indicators:** 4px radius or fully pill-shaped for chips.

## Components

### Buttons
Primary buttons use the Teal (#008080) background with White text. They must be full-width on mobile to maximize hit area. Secondary buttons use a Deep Navy outline with a 2px stroke.

### Cards
Cards are the primary vehicle for wellbeing data. They feature a 16px corner radius, a white surface, and a subtle 1px border in a light grey (#E0E0E0) to ensure definition against the off-white background.

### Input Fields
Inputs use a 16px internal padding and an 8px radius. The border turns Teal (#008080) when focused. Error states must include both a red border and an icon for accessibility.

### Bottom Navigation
A fixed navigation bar using Deep Navy (#003366) for icons and labels. The active state is indicated by a Teal highlight. This provides a constant anchor for the user to return to "Home" or "Support."

### Monitoring Chips
Small, pill-shaped indicators used to show status (e.g., "Stable," "Pending"). Use low-saturation background tints of the primary color to keep them readable but secondary to the main text.

### Accessible Icons
Icons must be "filled" rather than thin-line styles to ensure they remain visible at lower brightness settings or for users with visual impairments.