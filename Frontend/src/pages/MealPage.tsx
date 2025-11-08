import { useState, useEffect, useRef, useCallback } from 'react';
import { Input, Button, message, InputNumber, Select, Modal, Table, Spin } from 'antd';
import { DeleteOutlined, CameraOutlined } from '@ant-design/icons';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import {
  getTodaySummary,
  getTodayMeals,
  deleteMeal,
  createMeal,
  getAllMeals,
  calculateNutrientGaps,
  estimateMealWithAI,
  type DailyIntakeSummary,
} from '../services/nutritionService';
import type { Meal } from '../types/HomePage';
import type { MealDetail } from '../types/Meal';
import MealHistoryList from '../components/MealHistoryList';

const { TextArea } = Input;

type MealSortOption = 'date_desc' | 'date_asc' | 'calories_desc' | 'calories_asc';

export default function MealPage() {
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState<DailyIntakeSummary | null>(null);
  const [meals, setMeals] = useState<Meal[]>([]);
  const [savedMeals, setSavedMeals] = useState<MealDetail[]>([]);
  const [mealDescription, setMealDescription] = useState('');
  const [estimating, setEstimating] = useState(false);
  const [photoUrl, setPhotoUrl] = useState<string>('');
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // History state
  const [historyMeals, setHistoryMeals] = useState<MealDetail[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [historySort, setHistorySort] = useState<MealSortOption>('date_desc');
  const [deletingMealId, setDeletingMealId] = useState<number | null>(null);

  // Edit meal form state
  const [editForm, setEditForm] = useState({
    selectedMeal: '',
    portion: 6,
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    role: 'lunch' as string,
  });

  // Edit modal state
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingMeal, setEditingMeal] = useState<Meal | null>(null);
  const [editModalForm, setEditModalForm] = useState({
    name: '',
    portion: 6,
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
  });

  useEffect(() => {
    loadData();
    loadSavedMeals();
    loadHistory();
  }, []);

  useEffect(() => {
    loadHistory();
  }, [historySort]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [summaryData, mealsData] = await Promise.all([
        getTodaySummary(),
        getTodayMeals(),
      ]);
      setSummary(summaryData);
      setMeals(mealsData);
    } catch (error) {
      message.error('Failed to load meal data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const loadSavedMeals = async () => {
    try {
      const savedMealsData = await getAllMeals();
      setSavedMeals(savedMealsData);
    } catch (error) {
      console.error('Failed to load saved meals:', error);
    }
  };

  const loadHistory = useCallback(async () => {
    setLoadingHistory(true);
    try {
      const allMeals = await getAllMeals();
      // Sort meals based on selected option
      let sortedMeals = [...allMeals];

      switch (historySort) {
        case 'date_desc':
          sortedMeals.sort((a, b) => {
            const dateA = a.mealTime ? new Date(a.mealTime).getTime() : 0;
            const dateB = b.mealTime ? new Date(b.mealTime).getTime() : 0;
            return dateB - dateA;
          });
          break;
        case 'date_asc':
          sortedMeals.sort((a, b) => {
            const dateA = a.mealTime ? new Date(a.mealTime).getTime() : 0;
            const dateB = b.mealTime ? new Date(b.mealTime).getTime() : 0;
            return dateA - dateB;
          });
          break;
        case 'calories_desc':
          sortedMeals.sort((a, b) => b.totalCalories - a.totalCalories);
          break;
        case 'calories_asc':
          sortedMeals.sort((a, b) => a.totalCalories - b.totalCalories);
          break;
      }

      setHistoryMeals(sortedMeals);
    } catch (error) {
      message.error('Failed to load meal history');
      console.error(error);
    } finally {
      setLoadingHistory(false);
    }
  }, [historySort]);

  const handleEstimate = async () => {
    console.log('🎯 [MealPage] handleEstimate called');

    // Validate: at least one input (description or image) is required
    if (!mealDescription.trim() && !selectedImageFile) {
      console.warn('⚠️ [MealPage] Validation failed: no description or image');
      message.warning('Please enter a meal description or upload a photo');
      return;
    }

    console.log('📋 [MealPage] Request details:', {
      hasImage: !!selectedImageFile,
      imageDetails: selectedImageFile ? {
        name: selectedImageFile.name,
        size: selectedImageFile.size,
        type: selectedImageFile.type
      } : null,
      description: mealDescription.trim() || null,
      role: editForm.role,
      save: false
    });

    setEstimating(true);
    try {
      console.log('🔄 [MealPage] Calling estimateMealWithAI...');
      // Call AI meal estimation API
      const response = await estimateMealWithAI(
        selectedImageFile,
        mealDescription.trim() || null,
        editForm.role,
        false // Don't save yet, let user review first
      );

      console.log('✅ [MealPage] Received response:', response);

      // Populate the edit form with AI estimates for user review
      setEditForm({
        selectedMeal: response.mealDescription,
        portion: 6,
        calories: Number(response.totalCalories),
        protein: Number(response.proteinG),
        carbs: Number(response.carbsG),
        fat: Number(response.fatG),
        role: response.role,
      });

      // Set the photo URL if available
      if (response.photoUrl) {
        console.log('🖼️ [MealPage] Setting photo URL:', response.photoUrl);
        setPhotoUrl(response.photoUrl);
      }

      message.success('AI estimation complete! Please review and adjust the values if needed.');
    } catch (error) {
      console.error('❌ [MealPage] Estimation failed:', error);
      message.error('Failed to estimate meal. Please try again.');
    } finally {
      setEstimating(false);
      console.log('✅ [MealPage] handleEstimate completed');
    }
  };

  const handleSaveMeal = async () => {
    if (!editForm.selectedMeal.trim()) {
      message.warning('Please enter a meal description');
      return;
    }

    if (editForm.calories <= 0) {
      message.warning('Please enter valid calorie amount');
      return;
    }

    try {
      await createMeal({
        mealDescription: editForm.selectedMeal,
        photoUrl: photoUrl || undefined,
        totalCalories: editForm.calories,
        proteinG: editForm.protein,
        carbsG: editForm.carbs,
        fatG: editForm.fat,
        role: editForm.role,
        isAiGenerated: false,
      });

      message.success('Meal saved successfully!');

      // Reset form
      setMealDescription('');
      setEditForm({
        selectedMeal: '',
        portion: 6,
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0,
        role: 'lunch',
      });
      setPhotoUrl('');
      setSelectedImageFile(null);

      // Reload data
      loadData();
      loadSavedMeals();
      loadHistory();
    } catch (error) {
      message.error('Failed to save meal');
      console.error(error);
    }
  };

  const handleMealSelect = (value: string) => {
    const selected = savedMeals.find(m => m.mealId === Number(value));
    if (selected) {
      setEditForm({
        selectedMeal: selected.mealDescription,
        portion: 6,
        calories: Number(selected.totalCalories),
        protein: Number(selected.proteinG),
        carbs: Number(selected.carbsG),
        fat: Number(selected.fatG),
        role: selected.role || 'lunch',
      });
      setMealDescription(selected.mealDescription);
    }
  };

  const handleDeleteMeal = async (mealId: string) => {
    try {
      await deleteMeal(Number(mealId));
      message.success('Meal deleted successfully');
      loadData();
    } catch (error) {
      message.error('Failed to delete meal');
      console.error(error);
    }
  };

  const handleDeleteHistoryMeal = async (mealId: number) => {
    setDeletingMealId(mealId);
    try {
      await deleteMeal(mealId);
      message.success('Meal deleted successfully');
      await Promise.all([loadData(), loadHistory()]);
    } catch (error) {
      message.error('Failed to delete meal');
      console.error(error);
    } finally {
      setDeletingMealId(null);
    }
  };

  const handleEditMeal = (meal: Meal) => {
    setEditingMeal(meal);
    setEditModalForm({
      name: meal.name,
      portion: 6,
      calories: meal.calories,
      protein: meal.protein,
      carbs: meal.carbs,
      fat: meal.fat,
    });
    setEditModalVisible(true);
  };

  const handleSaveEdit = async () => {
    if (!editingMeal) return;

    try {
      // TODO: Implement meal update endpoint
      message.success('Meal updated successfully!');
      setEditModalVisible(false);
      loadData();
    } catch (error) {
      message.error('Failed to update meal');
      console.error(error);
    }
  };

  const handlePhotoUpload = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    console.log('📁 [MealPage] File input changed');
    const file = event.target.files?.[0];
    if (!file) {
      console.warn('⚠️ [MealPage] No file selected');
      return;
    }

    console.log('📸 [MealPage] File selected:', {
      name: file.name,
      size: `${(file.size / 1024).toFixed(2)} KB`,
      type: file.type,
      lastModified: new Date(file.lastModified).toISOString()
    });

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      console.error('❌ [MealPage] Invalid file type:', file.type);
      message.error('Please upload a valid image file (JPEG, PNG, or WebP)');
      return;
    }

    // Validate file size (max 10MB)
    const maxSize = 10 * 1024 * 1024; // 10MB in bytes
    if (file.size > maxSize) {
      console.error('❌ [MealPage] File too large:', file.size, 'bytes');
      message.error('Image file size must be less than 10MB');
      return;
    }

    // Create preview URL
    const previewUrl = URL.createObjectURL(file);
    console.log('🖼️ [MealPage] Preview URL created:', previewUrl);
    setPhotoUrl(previewUrl);
    setSelectedImageFile(file);
    console.log('✅ [MealPage] Image file saved to state');
    message.success('Image selected successfully');
  };

  const totals = meals.reduce(
    (acc, meal) => ({
      calories: acc.calories + meal.calories,
      protein: acc.protein + meal.protein,
      carbs: acc.carbs + meal.carbs,
      fat: acc.fat + meal.fat,
    }),
    { calories: 0, protein: 0, carbs: 0, fat: 0 }
  );

  const pieData = summary
    ? [
        {
          name: 'Protein',
          value: summary.currentIntake.protein,
          percentage: ((summary.currentIntake.protein * 4) / summary.currentIntake.calories * 100).toFixed(1),
        },
        {
          name: 'Carbs',
          value: summary.currentIntake.carbs,
          percentage: ((summary.currentIntake.carbs * 4) / summary.currentIntake.calories * 100).toFixed(1),
        },
        {
          name: 'Fats',
          value: summary.currentIntake.fat,
          percentage: ((summary.currentIntake.fat * 9) / summary.currentIntake.calories * 100).toFixed(1),
        },
      ]
    : [];

  const COLORS = ['#3b82f6', '#93c5fd', '#dbeafe'];

  const nutrientGaps = summary ? calculateNutrientGaps(summary) : null;
  const gapsData = nutrientGaps
    ? [
        { name: 'Protein', current: nutrientGaps.protein.current, gap: nutrientGaps.protein.gap, total: nutrientGaps.protein.target },
        { name: 'Carbs', current: nutrientGaps.carbs.current, gap: nutrientGaps.carbs.gap, total: nutrientGaps.carbs.target },
        { name: 'Fat', current: nutrientGaps.fat.current, gap: nutrientGaps.fat.gap, total: nutrientGaps.fat.target },
      ]
    : [];

  const mealColumns = [
    {
      title: 'Meal',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: Meal) => (
        <button
          onClick={() => handleEditMeal(record)}
          style={{ textAlign: 'left', color: '#2563eb', cursor: 'pointer', background: 'none', border: 'none', padding: 0 }}
          onMouseEnter={(e) => (e.currentTarget.style.textDecoration = 'underline')}
          onMouseLeave={(e) => (e.currentTarget.style.textDecoration = 'none')}
        >
          {text}
        </button>
      ),
    },
    {
      title: 'Calories (cal)',
      dataIndex: 'calories',
      key: 'calories',
      align: 'center' as const,
    },
    {
      title: 'Protein (g)',
      dataIndex: 'protein',
      key: 'protein',
      align: 'center' as const,
    },
    {
      title: 'Carbs (g)',
      dataIndex: 'carbs',
      key: 'carbs',
      align: 'center' as const,
    },
    {
      title: 'Fat (g)',
      dataIndex: 'fat',
      key: 'fat',
      align: 'center' as const,
    },
    {
      title: 'Actions',
      key: 'action',
      width: 80,
      align: 'center' as const,
      render: (_: unknown, record: Meal) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleDeleteMeal(record.id)}
        />
      ),
    },
  ];

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '24rem', alignItems: 'center', justifyContent: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ padding: '0' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <div style={{ fontSize: '2.25rem' }}>🍽️</div>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 600, color: '#1e293b', margin: 0 }}>Diet</h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>
        {/* Main container with two columns */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
          {/* Left Column - 2/3 width */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* AI Meal Estimation */}
            <div style={{ borderRadius: '16px', background: 'white', padding: '24px', boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1)' }}>
              <h2 style={{ marginBottom: '16px', fontSize: '1.25rem', fontWeight: 600, color: '#1e293b' }}>AI Meal Estimation</h2>

              <div style={{ display: 'flex', gap: '24px' }}>
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  <div>
                    <label style={{ marginBottom: '8px', display: 'block', fontSize: '0.875rem', fontWeight: 500, color: '#334155' }}>
                      Add Meal
                    </label>
                    <TextArea
                      placeholder="description"
                      value={mealDescription}
                      onChange={(e) => setMealDescription(e.target.value)}
                      rows={4}
                      style={{ width: '100%' }}
                    />
                  </div>
                  <Button
                    type="primary"
                    onClick={handleEstimate}
                    loading={estimating}
                    style={{ width: '128px' }}
                  >
                    Estimate
                  </Button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '12px' }}>
                  <div style={{ display: 'flex', height: '160px', width: '160px', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', borderRadius: '8px', border: '2px solid #e2e8f0' }}>
                    {photoUrl ? (
                      <img
                        src={photoUrl}
                        alt="Meal"
                        style={{ height: '100%', width: '100%', objectFit: 'cover' }}
                      />
                    ) : (
                      <div style={{ display: 'flex', height: '100%', width: '100%', alignItems: 'center', justifyContent: 'center', background: '#f1f5f9' }}>
                        <span style={{ fontSize: '4rem' }}>🍽️</span>
                      </div>
                    )}
                  </div>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/jpeg,image/jpg,image/png,image/webp"
                    onChange={handleFileChange}
                    style={{ display: 'none' }}
                  />
                  <Button
                    type="default"
                    icon={<CameraOutlined />}
                    onClick={handlePhotoUpload}
                    size="small"
                  >
                    Upload Photo
                  </Button>
                  {photoUrl && (
                    <Button
                      type="text"
                      danger
                      size="small"
                      onClick={() => {
                        setPhotoUrl('');
                        setSelectedImageFile(null);
                      }}
                    >
                      Remove Photo
                    </Button>
                  )}
                </div>
              </div>
            </div>

            {/* Today's Meals Table */}
            <div style={{ borderRadius: '16px', background: 'white', padding: '24px', boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1)' }}>
              <h2 style={{ marginBottom: '16px', fontSize: '1.25rem', fontWeight: 600, color: '#1e293b' }}>Today's Meals</h2>
              <Table
                columns={mealColumns}
                dataSource={meals}
                rowKey="id"
                pagination={false}
                summary={() => (
                  <Table.Summary>
                    <Table.Summary.Row style={{ background: '#f8fafc', fontWeight: 600 }}>
                      <Table.Summary.Cell index={0}>Total</Table.Summary.Cell>
                      <Table.Summary.Cell index={1} align="center">
                        {totals.calories}
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={2} align="center">
                        {totals.protein}
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={3} align="center">
                        {totals.carbs}
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={4} align="center">
                        {totals.fat}
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={5} />
                    </Table.Summary.Row>
                  </Table.Summary>
                )}
              />
            </div>

            {/* Daily Intake Chart */}
            <div style={{ borderRadius: '16px', background: 'white', padding: '24px', boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1)' }}>
              <h2 style={{ marginBottom: '16px', fontSize: '1.25rem', fontWeight: 600, color: '#1e293b' }}>Daily Intake</h2>
              <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'flex-start' }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '3.75rem', fontWeight: 700, color: '#1e293b' }}>
                    {summary?.currentIntake.calories || 0}
                  </div>
                  <div style={{ marginTop: '4px', fontSize: '1.125rem', color: '#64748b' }}>
                    /{summary?.dailyTargets.calories || 2000} cal
                  </div>
                </div>
                <div style={{ flex: 1, marginLeft: '32px' }}>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={100}
                        paddingAngle={5}
                        dataKey="value"
                        label={({ name, percentage }) => `${name}\n${percentage}%`}
                      >
                        {pieData.map((_entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - 1/3 width */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Edit Meal Panel */}
            <div style={{ borderRadius: '16px', background: 'white', padding: '24px', boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1)' }}>
              <h2 style={{ marginBottom: '16px', fontSize: '1.25rem', fontWeight: 600, color: '#1e293b' }}>Edit Meal</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Select Saved Meal</label>
                  <Select
                    value={editForm.selectedMeal || undefined}
                    placeholder="Select saved meal"
                    style={{ width: '100%' }}
                    onChange={handleMealSelect}
                    options={savedMeals.map(meal => ({
                      value: String(meal.mealId),
                      label: meal.mealDescription
                    }))}
                    allowClear
                    onClear={() => setEditForm({ ...editForm, selectedMeal: '' })}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Meal Name</label>
                  <Input
                    value={editForm.selectedMeal}
                    onChange={(e) => setEditForm({ ...editForm, selectedMeal: e.target.value })}
                    placeholder="Enter meal name"
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Meal Type</label>
                  <Select
                    value={editForm.role}
                    onChange={(val) => setEditForm({ ...editForm, role: val })}
                    style={{ width: '100%' }}
                    options={[
                      { value: 'breakfast', label: 'Breakfast' },
                      { value: 'lunch', label: 'Lunch' },
                      { value: 'dinner', label: 'Dinner' },
                      { value: 'snack', label: 'Snack' },
                    ]}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Portion</label>
                  <InputNumber
                    value={editForm.portion}
                    onChange={(val) => setEditForm({ ...editForm, portion: val || 0 })}
                    style={{ width: '100%' }}
                    addonAfter="oz"
                    min={0}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Calories</label>
                  <InputNumber
                    value={editForm.calories}
                    onChange={(val) => setEditForm({ ...editForm, calories: val || 0 })}
                    style={{ width: '100%' }}
                    min={0}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Protein</label>
                  <InputNumber
                    value={editForm.protein}
                    onChange={(val) => setEditForm({ ...editForm, protein: val || 0 })}
                    style={{ width: '100%' }}
                    addonAfter="g"
                    min={0}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Carbs</label>
                  <InputNumber
                    value={editForm.carbs}
                    onChange={(val) => setEditForm({ ...editForm, carbs: val || 0 })}
                    style={{ width: '100%' }}
                    addonAfter="g"
                    min={0}
                  />
                </div>

                <div>
                  <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Fat</label>
                  <InputNumber
                    value={editForm.fat}
                    onChange={(val) => setEditForm({ ...editForm, fat: val || 0 })}
                    style={{ width: '100%' }}
                    addonAfter="g"
                    min={0}
                  />
                </div>

                <Button
                  type="primary"
                  onClick={handleSaveMeal}
                  style={{ width: '100%' }}
                >
                  Save
                </Button>
              </div>
            </div>

            {/* Nutrient Gaps Chart */}
            <div style={{ borderRadius: '16px', background: 'white', padding: '24px', boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1)' }}>
              <h2 style={{ marginBottom: '16px', fontSize: '1.25rem', fontWeight: 600, color: '#1e293b' }}>Nutrient Gaps (g)</h2>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={gapsData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="current" stackId="a" fill="#5b8def" name="Current" />
                  <Bar dataKey="gap" stackId="a" fill="#bfdbfe" name="Gap" />
                </BarChart>
              </ResponsiveContainer>

              {/* Gap Summary */}
              {nutrientGaps && (
                <div style={{ marginTop: '16px', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px', textAlign: 'center' }}>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Protein Gap</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#f97316' }}>
                      {nutrientGaps.protein.gap.toFixed(0)} g
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Carbs Gap</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#f97316' }}>
                      {nutrientGaps.carbs.gap.toFixed(0)} g
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Fat Gap</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#f97316' }}>
                      {nutrientGaps.fat.gap.toFixed(0)} g
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Meal History */}
      <div style={{ marginTop: '24px' }}>
        <MealHistoryList
          meals={historyMeals}
          loading={loadingHistory}
          sortBy={historySort}
          onSortChange={setHistorySort}
          onDelete={handleDeleteHistoryMeal}
          deleteInProgressId={deletingMealId}
        />
      </div>

      {/* Edit Meal Modal */}
      <Modal
        title="Edit Meal"
        open={editModalVisible}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalVisible(false)}
        okText="Save"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', padding: '16px 0' }}>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Meal Name</label>
            <Input
              value={editModalForm.name}
              onChange={(e) => setEditModalForm({ ...editModalForm, name: e.target.value })}
            />
          </div>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Portion</label>
            <InputNumber
              value={editModalForm.portion}
              onChange={(val) => setEditModalForm({ ...editModalForm, portion: val || 0 })}
              style={{ width: '100%' }}
              addonAfter="oz"
            />
          </div>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Calories</label>
            <InputNumber
              value={editModalForm.calories}
              onChange={(val) => setEditModalForm({ ...editModalForm, calories: val || 0 })}
              style={{ width: '100%' }}
            />
          </div>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Protein (g)</label>
            <InputNumber
              value={editModalForm.protein}
              onChange={(val) => setEditModalForm({ ...editModalForm, protein: val || 0 })}
              style={{ width: '100%' }}
            />
          </div>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Carbs (g)</label>
            <InputNumber
              value={editModalForm.carbs}
              onChange={(val) => setEditModalForm({ ...editModalForm, carbs: val || 0 })}
              style={{ width: '100%' }}
            />
          </div>
          <div>
            <label style={{ marginBottom: '4px', display: 'block', fontSize: '0.875rem', color: '#475569' }}>Fat (g)</label>
            <InputNumber
              value={editModalForm.fat}
              onChange={(val) => setEditModalForm({ ...editModalForm, fat: val || 0 })}
              style={{ width: '100%' }}
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}
