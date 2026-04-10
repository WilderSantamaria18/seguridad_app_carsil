# CARSIL - Guía de Diseño UI (Flat & Minimalist)

## Estilo: Flat Design & Minimalist UI — Profesional e Industrial

---

> [!IMPORTANT]
> **Filosofía del Diseño:**
> El sistema SGE CARSIL debe seguir una estética **Flat Design** (diseño plano) y **Minimalist UI**. Esto implica eliminar cualquier elemento que no aporte valor funcional, evitando texturas, sombras pesadas o degradados complejos. La interfaz debe sentirse "limpia", "ligera" y "enfocada en el contenido".

---

> [!IMPORTANT]
> **Reglas Obligatorias:**
> - **Sin Emojis**: Usar exclusivamente Bootstrap Icons (`bi-`).
> - **Sin Degradados**: Colores 100% sólidos para fondos, botones y acentos.
> - **Espacio en Blanco (Negative Space)**: Priorizar márgenes generosos para evitar la saturación visual.
> - **Bordes Finos**: Usar bordes de `1px` en lugar de sombras para delimitar contenedores.
> - **Tipografía Inter**: Fuente de sistema moderna, legible y profesional.

---

## Paleta de Colores (Sólidos & Corporativos)

| Token                | Valor       | Uso Principal                           |
|----------------------|-------------|------------------------------------------|
| `--primary`          | `#3B49DF`   | Botones principales, enlaces, acentos    |
| `--primary-dark`     | `#2d38b0`   | Hover de botones (cambio de tono plano)  |
| `--primary-light`    | `#EEF0FD`   | Fondos de badges, hover de filas         |
| `--bg-page`          | `#F7F7F8`   | Fondo general de la aplicación           |
| `--bg-card`          | `#FFFFFF`   | Tarjetas, contenedores, tablas           |
| `--border`           | `#E5E5E5`   | Divisores de 1px (Sin sombras)           |
| `--text-primary`     | `#1A1A2E`   | Títulos y contenido principal            |
| `--text-secondary`   | `#6B7280`   | Etiquetas, subtítulos, placeholders      |
| `--success`          | `#10B981`   | Estados activos, éxito, inventario OK    |
| `--warning`          | `#F59E0B`   | Alertas de stock bajo, pendientes        |
| `--danger`           | `#EF4444`   | Errores, eliminación, anulación          |

---

## Tipografía (Jerarquía Visual)

- **Fuente**: `'Inter', sans-serif` (Google Fonts)
- **Carga Visual**:
  - **H1 / Títulos**: `1.5rem` (24px), Bold (700). Color: `--text-primary`.
  - **H2 / Secciones**: `1.1rem` (18px), SemiBold (600).
  - **Body / Tablas**: `0.875rem` (14px), Regular (400). Color: `--text-secondary`.
  - **Labels / Inputs**: `0.75rem` (12px), Medium (500), Uppercase.

---

## Componentes Flat Design

### 1. Botones (Buttons)
- **Forma**: Border-radius de `8px`.
- **Efecto**: Sin sombras. El hover solo cambia el color de fondo a un tono más oscuro o claro de forma instantánea o con transición rápida (150ms).
- **Padding**: `10px 20px` para botones de acción.

### 2. Tarjetas (Cards)
- **Estilo**: Fondo blanco sólido, borde gris muy fino (`1px solid var(--border)`).
- **Sombra**: **Prohibido** usar sombras elevadas. Solo se permite una sombra extremadamente sutil (`box-shadow: 0 1px 2px rgba(0,0,0,0.05)`) para separar del fondo.

### 3. Tablas (Data Tables)
- **Diseño**: Sin bordes verticales. Solo líneas horizontales finas entre filas.
- **Hover**: Cambio de color de fondo de la fila a `--primary-light` al pasar el mouse.
- **Badges**: Colores sólidos, sin bordes, texto en negrita pequeña.

---

## Reglas de Implementación (Contexto CRM/ERP)

1. **Jerarquía por Color**: Usar el azul `--primary` solo para la acción principal de cada página (ej: "Crear Proforma").
2. **Iconografía Minimalista**: Los iconos deben ser de línea fina (outline), nunca rellenos (solid) a menos que sea para un estado activo.
3. **Inputs y Formularios**: Fondo blanco, borde de 1px. En `:focus`, cambiar el color del borde a `--primary` (sin "glow" o resplandor).
4. **Dashboard Semántico**: Los gráficos deben usar colores planos (no degradados) y leyendas claras.
5. **Responsive**: Todo contenedor debe adaptarse usando Flexbox o Grid, manteniendo el alineamiento "limpio".
6. **Notificaciones**: Toasts planos, sin sombras, con color sólido en el lateral izquierdo indicando el tipo de alerta.

---

## Resumen de Estética para Generación:
"Interfaces ultra-limpias, profesionales, de estilo industrial/tech. Eliminación de ruido visual. Foco en los datos y la rapidez de lectura. El blanco y el gris claro dominan, con acentos azules cargados de significado funcional."
maticamente via middleware
