import QtQuick

QtObject {
    id: theme

    // Palette matching screenshot
    readonly property color sidebarBg: "#3A47D5"
    readonly property color sidebarActive: "#4F5AE8"
    readonly property color sidebarHover: "#4853DE"
    readonly property color sidebarText: "#FFFFFF"
    readonly property color sidebarTextMuted: "#BAC3FF"

    readonly property color appBg: "#F4F6FB"
    readonly property color cardBg: "#FFFFFF"
    readonly property color cardBorder: "#E8EEF5"
    
    readonly property color textPrimary: "#1E293B"
    readonly property color textSecondary: "#64748B"
    readonly property color textMuted: "#94A3B8"

    readonly property color primaryBlue: "#3A47D5"
    readonly property color chartLine: "#3B4CE8"
    readonly property color chartGradientTop: "#333B4CE8"
    readonly property color chartGradientBottom: "#023B4CE8"

    // Stat card icon background tints
    readonly property color iconBgLavender: "#EDE9FE"
    readonly property color iconBgCyan: "#E0F2FE"
    readonly property color iconBgGreen: "#DCFCE7"
    readonly property color iconBgPurple: "#F3E8FF"

    // Stat card icon colors
    readonly property color iconColorLavender: "#6D28D9"
    readonly property color iconColorCyan: "#0284C7"
    readonly property color iconColorGreen: "#16A34A"
    readonly property color iconColorPurple: "#7C3AED"

    // Status Badges
    readonly property color statusInProgressBg: "#FEF3C7"
    readonly property color statusInProgressText: "#B45309"
    readonly property color statusResolvedBg: "#DCFCE7"
    readonly property color statusResolvedText: "#15803D"
    readonly property color statusCriticalBg: "#FEE2E2"
    readonly property color statusCriticalText: "#B91C1C"
    readonly property color statusPendingBg: "#F1F5F9"
    readonly property color statusPendingText: "#475569"

    // Category progress colors
    readonly property color catInfrastructure: "#3A47D5"
    readonly property color catPublicSafety: "#3B82F6"
    readonly property color catSanitation: "#06B6D4"
    readonly property color catEnvironment: "#38BDF8"

    // Dimensions & Radii
    readonly property int radiusSm: 6
    readonly property int radiusMd: 10
    readonly property int radiusLg: 14
    readonly property int radiusXl: 18

    readonly property string fontFamily: "Segoe UI, Inter, -apple-system, sans-serif"
}
