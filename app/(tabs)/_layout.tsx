import React from 'react';
import FontAwesome from '@expo/vector-icons/FontAwesome';
import { Tabs } from 'expo-router';
import { View, StyleSheet, Platform } from 'react-native';
import { useTheme } from '@/constants/Theme';

function TabIcon(props: { name: React.ComponentProps<typeof FontAwesome>['name']; color: string; focused: boolean }) {
  return (
    <View style={[st.icon, props.focused && st.iconFocused]}>
      <FontAwesome size={19} name={props.name} color={props.color} />
    </View>
  );
}

export default function TabLayout() {
  const { theme, isDark } = useTheme();

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: theme.system.blue,
        tabBarInactiveTintColor: theme.colors.textMuted,
        tabBarStyle: {
          backgroundColor: isDark ? '#12122E' : '#FFFFFF',
          borderTopColor: theme.colors.border,
          borderTopWidth: 1,
          height: Platform.OS === 'ios' ? 80 : 56,
          paddingBottom: Platform.OS === 'ios' ? 24 : 6,
          paddingTop: 6,
          elevation: 12,
          shadowColor: isDark ? '#3B82F6' : '#94A3B8',
          shadowOffset: { width: 0, height: -2 },
          shadowOpacity: 0.08,
          shadowRadius: 8,
        },
        tabBarLabelStyle: { fontSize: 9, fontWeight: '600', marginTop: 1 },
        headerShown: false,
      }}>
      <Tabs.Screen name="index" options={{ title: 'Home', tabBarIcon: ({ color, focused }) => <TabIcon name="home" color={color} focused={focused} /> }} />
      <Tabs.Screen name="quests" options={{ title: 'Quests', tabBarIcon: ({ color, focused }) => <TabIcon name="list-alt" color={color} focused={focused} /> }} />
      <Tabs.Screen name="skills" options={{ title: 'Skills', tabBarIcon: ({ color, focused }) => <TabIcon name="sitemap" color={color} focused={focused} /> }} />
      <Tabs.Screen name="finance" options={{ title: 'Finance', tabBarIcon: ({ color, focused }) => <TabIcon name="line-chart" color={color} focused={focused} /> }} />
      <Tabs.Screen name="more" options={{ title: 'More', tabBarIcon: ({ color, focused }) => <TabIcon name="th-large" color={color} focused={focused} /> }} />
    </Tabs>
  );
}

const st = StyleSheet.create({
  icon: { alignItems: 'center', justifyContent: 'center', width: 32, height: 32, borderRadius: 16 },
  iconFocused: { backgroundColor: 'rgba(59, 130, 246, 0.1)' },
});
