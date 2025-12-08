import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Booking, BookingUpdateRequest } from '../types';
import { apiService } from '../services/api';
import { Calendar, Users, ArrowLeft, Clock, Mail, Edit } from 'lucide-react';
import toast from 'react-hot-toast';

const BookingDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [newTicketCount, setNewTicketCount] = useState(1);

  const loadBooking = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await apiService.getBooking(parseInt(id, 10));
      setBooking(data);
    } catch (error) {
      console.error('Error loading booking:', error);
      toast.error('Бронирование не найдено');
      navigate('/bookings');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => {
    loadBooking();
  }, [loadBooking]);

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleEditBooking = () => {
    if (!booking) return;
    setNewTicketCount(booking.ticketCount);
    setShowEditModal(true);
  };

  const handleConfirmEdit = async () => {
    if (!booking) return;

    if (newTicketCount === booking.ticketCount) {
      setShowEditModal(false);
      return;
    }

    if (newTicketCount < 1 || newTicketCount > 10) {
      toast.error('Количество билетов должно быть от 1 до 10');
      return;
    }

    try {
      setLoading(true);
      const updateRequest: BookingUpdateRequest = {
        ticketCount: newTicketCount,
      };
      
      await apiService.updateBooking(booking.id, updateRequest);
      
      toast.success('Количество билетов успешно обновлено!');
      setShowEditModal(false);
      loadBooking();
    } catch (error: any) {
      console.error('Error updating booking:', error);
      const message = error.response?.data?.message || 'Ошибка при обновлении бронирования';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (!booking) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium text-gray-900">Бронирование не найдено</h3>
        <button
          onClick={() => navigate('/bookings')}
          className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Назад к бронированиям
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6 flex items-center justify-between">
        <button
          onClick={() => navigate('/bookings')}
          className="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-500"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Назад к бронированиям
        </button>
        <span className="text-sm text-gray-500">Номер бронирования: #{booking.id}</span>
      </div>

      <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        <div className="p-6 sm:p-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-4">
            {booking.event.title}
          </h1>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-gray-900">Информация о бронировании</h2>

              <div className="flex items-center text-gray-600">
                <Calendar className="w-5 h-5 mr-3" />
                <span>Дата и время мероприятия: {formatDateTime(booking.event.dateTime)}</span>
              </div>

              <div className="flex items-center text-gray-600">
                <Users className="w-5 h-5 mr-3" />
                <span>Забронировано билетов: {booking.ticketCount}</span>
              </div>

              <div className="flex items-center text-gray-600">
                <Clock className="w-5 h-5 mr-3" />
                <span>Создано: {formatDateTime(booking.createdAt)}</span>
              </div>

              {booking.expiryTime && !booking.confirmed && (
                <div className="flex items-center text-orange-600">
                  <Clock className="w-5 h-5 mr-3" />
                  <span>
                    Время на подтверждение: {formatDateTime(booking.expiryTime)}
                  </span>
                </div>
              )}

              <div className="flex items-center text-gray-600">
                <Mail className="w-5 h-5 mr-3" />
                <span>Email: {booking.customerEmail}</span>
              </div>

              <div className="mt-4">
                <span
                  className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${
                    booking.confirmed
                      ? 'bg-green-100 text-green-800'
                      : 'bg-yellow-100 text-yellow-800'
                  }`}
                >
                  {booking.confirmed ? 'Подтверждено' : 'Ожидает подтверждения'}
                </span>
              </div>
            </div>

            <div>
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Информация о событии</h2>
              <p className="text-gray-700 mb-4">{booking.event.description}</p>

              {booking.event.coverUrl && (
                <div className="mt-4 rounded-lg overflow-hidden border border-gray-200">
                  <img
                    src={booking.event.coverUrl}
                    alt={booking.event.title}
                    className="w-full h-48 object-cover"
                  />
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Модальное окно для изменения количества билетов */}
      {showEditModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Изменить количество билетов
              </h3>
              
              <p className="text-sm text-gray-600 mb-4">
                Текущее количество: <strong>{booking.ticketCount}</strong> билетов
              </p>
              <p className="text-sm text-gray-600 mb-4">
                Максимально можно забронировать: <strong>10</strong> билетов
              </p>
              
              <div className="mb-4">
                <label htmlFor="ticketCount" className="block text-sm font-medium text-gray-700 mb-2">
                  Новое количество билетов:
                </label>
                <input
                  type="number"
                  id="ticketCount"
                  min="1"
                  max="10"
                  value={newTicketCount}
                  onChange={(e) => {
                    const value = parseInt(e.target.value);
                    if (value >= 1 && value <= 10) {
                      setNewTicketCount(value);
                    }
                  }}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              
              <div className="flex justify-end space-x-3">
                <button
                  onClick={() => {
                    setShowEditModal(false);
                  }}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
                >
                  Отмена
                </button>
                <button
                  onClick={handleConfirmEdit}
                  disabled={loading || newTicketCount === booking.ticketCount}
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {loading ? 'Обновление...' : 'Сохранить'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingDetail;




