export const Theme = {
    colors: {
        primary: '#1E3A5F',       // deep navy
        accent: '#F59E0B',        // warm amber
        success: '#10B981',
        danger: '#EF4444',
        background: '#F7F8FA',
        surface: '#FFFFFF',
        text: '#1A202C',
        textMuted: '#6B7280',
        border: '#E5E7EB',
    },
    spacing: {
        xs: 4,
        sm: 8,
        md: 16,
        lg: 24,
        xl: 32,
    },
    borderRadius: {
        sm: 6,
        md: 14,                   // prescribed radius
        lg: 20,
        round: 50,
    },
    shadows: {
        soft: {
            shadowColor: '#000',
            shadowOffset: { width: 0, height: 2 },
            shadowOpacity: 0.07,
            shadowRadius: 16,
            elevation: 3,
        },
        deep: {
            shadowColor: '#000',
            shadowOffset: { width: 0, height: 4 },
            shadowOpacity: 0.12,
            shadowRadius: 20,
            elevation: 8,
        }
    },
    typography: {
        header: 'System', // Preferred: DM Serif Display
        body: 'System',   // Preferred: DM Sans / Nunito
    }
};
