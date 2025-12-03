import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Booking, BookingUpdateRequest } from '../types';
import { apiService } from '../services/api';
import { Calendar, Users, Clock, Trash2, Check, X, Edit } from 'lucide-react';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const BookingList: React.FC = () => {
  const navigate = useNavigate();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingBookingId, setEditingBookingId] = useState<number | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [newTicketCount, setNewTicketCount] = useState(1);
  const previousBookingsRef = useRef<Booking[]>([]);

  const loadBookings = useCallback(async () => {
    try {
      setLoading(true);
      const bookingsData = await apiService.getBookings();
      
      // Проверяем изменения статуса подтверждения для показа уведомлений
      if (previousBookingsRef.current.length > 0) {
        bookingsData.forEach((newBooking) => {
          const oldBooking = previousBookingsRef.current.find(b => b.id === newBooking.id);
          if (oldBooking && !oldBooking.confirmed && newBooking.confirmed) {
            toast.success(
              `🎉 Ваше бронирование на "${newBooking.event.title}" подтверждено!`,
              { duration: 5000 }
            );
          }
        });
      }
      
      previousBookingsRef.current = bookingsData;
      setBookings(bookingsData);
    } catch (error) {
      console.error('Error loading bookings:', error);
      toast.error('Ошибка при загрузке бронирований');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBookings();
    // Периодически проверяем изменения статуса (каждые 10 секунд)
    const interval = setInterval(() => {
      loadBookings();
    }, 10000);
    
    return () => clearInterval(interval);
  }, [loadBookings]);

  const handleDeleteBooking = async (bookingId: number, eventTitle: string, ticketCount: number) => {
    if (!window.confirm(`Отменить вашу бронь на ${ticketCount} билетов?`)) {
      return;
    }

    try {
      await apiService.deleteBooking(bookingId);
      toast.success('Бронирование отменено');
      loadBookings();
    } catch (error) {
      console.error('Error deleting booking:', error);
      toast.error('Ошибка при отмене бронирования');
    }
  };

  const handleEditBooking = (booking: Booking) => {
    setEditingBookingId(booking.id);
    setNewTicketCount(booking.ticketCount);
    setShowEditModal(true);
  };

  const handleConfirmEdit = async () => {
    if (!editingBookingId) return;

    const booking = bookings.find(b => b.id === editingBookingId);
    if (!booking) return;

    if (newTicketCount === booking.ticketCount) {
      setShowEditModal(false);
      setEditingBookingId(null);
      return;
    }

    if (newTicketCount > 10) {
      toast.error('Максимально можно забронировать 10 билетов');
      return;
    }

    try {
      setLoading(true);
      const updateRequest: BookingUpdateRequest = {
        ticketCount: newTicketCount,
      };
      
      await apiService.updateBooking(editingBookingId, updateRequest);
      
      toast.success('Количество билетов успешно обновлено!');
      setShowEditModal(false);
      setEditingBookingId(null);
      loadBookings();
    } catch (error: any) {
      console.error('Error updating booking:', error);
      const message = error.response?.data?.message || 'Ошибка при обновлении бронирования';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (confirmed: boolean) => {
    return confirmed ? (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
        <Check className="w-3 h-3 mr-1" />
        Подтверждено
      </span>
    ) : (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
        <X className="w-3 h-3 mr-1" />
        Ожидает подтверждения
      </span>
    );
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Мои бронирования</h1>
      </div>

      {bookings.length === 0 ? (
        <div className="text-center py-12">
          <Calendar className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">Нет бронирований</h3>
          <p className="mt-1 text-sm text-gray-500">
            У вас пока нет забронированных билетов.
          </p>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-gray-200">
            {bookings.map((booking) => (
              <li key={booking.id}>
                <div
                  className="px-4 py-4 sm:px-6 cursor-pointer hover:bg-gray-50 transition-colors"
                  onClick={() => navigate(`/bookings/${booking.id}`)}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="flex-shrink-0">
                        <Users className="h-8 w-8 text-gray-400" />
                      </div>
                      <div className="ml-4">
                        <div className="flex items-center">
                          <p className="text-sm font-medium text-gray-900">
                            {booking.event.title}
                          </p>
                          <div className="ml-2">
                            {getStatusBadge(booking.confirmed)}
                          </div>
                        </div>
                        <div className="mt-2 flex items-center text-sm text-gray-500 space-x-4">
                          <div className="flex items-center">
                            <Calendar className="w-4 h-4 mr-1" />
                            {formatDateTime(booking.event.dateTime)}
                          </div>
                          <div className="flex items-center">
                            <Users className="w-4 h-4 mr-1" />
                            {booking.ticketCount} билетов
                          </div>
                          <div className="flex items-center">
                            <Clock className="w-4 h-4 mr-1" />
                            Создано: {formatDateTime(booking.createdAt)}
                          </div>
                        </div>
                        {booking.expiryTime && !booking.confirmed && (
                          <div className="mt-2 text-sm text-orange-600">
                            Время на подтверждение: {formatDateTime(booking.expiryTime)}
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      {!booking.confirmed && (
                        <>
                          <button
                            onClick={() => handleEditBooking(booking)}
                            className="inline-flex items-center p-2 border border-transparent text-sm leading-4 font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                            title="Изменить количество билетов"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteBooking(booking.id, booking.event.title, booking.ticketCount)}
                            className="inline-flex items-center p-2 border border-transparent text-sm leading-4 font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                            title="Отменить бронирование"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Модальное окно для изменения количества билетов */}
      {showEditModal && editingBookingId && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Изменить количество билетов
              </h3>
              
              {(() => {
                const booking = bookings.find(b => b.id === editingBookingId);
                if (!booking) return null;
                
                const maxTickets = 10;
                
                return (
                  <>
                    <p className="text-sm text-gray-600 mb-4">
                      Текущее количество: <strong>{booking.ticketCount}</strong> билетов
                    </p>
                    <p className="text-sm text-gray-600 mb-4">
                      Максимально можно забронировать: <strong>{maxTickets}</strong> билетов
                    </p>
                    
                    <div className="mb-4">
                      <label htmlFor="ticketCount" className="block text-sm font-medium text-gray-700 mb-2">
                        Новое количество билетов:
                      </label>
                      <input
                        type="number"
                        id="ticketCount"
                        min="1"
                        max={maxTickets}
                        value={newTicketCount}
                        onChange={(e) => {
                          const value = parseInt(e.target.value);
                          if (value >= 1 && value <= maxTickets) {
                            setNewTicketCount(value);
                          }
                        }}
                        className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                      />
                    </div>
                    
                    <div className="flex justify-end space-x-3">
                      <button
                        onClick={() => {
                          setShowEditModal(false);
                          setEditingBookingId(null);
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
                  </>
                );
              })()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingList; 