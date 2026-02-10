import { useEffect, useState } from 'react';
import axiosInstance from '../services/axiosInstance';

export default function useCountries() {
  const [countries, setCountries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;

    const fetchCountries = async () => {
      try {
        setLoading(true);
        const res = await axiosInstance.get('/countries');
        const list = Array.isArray(res.data) ? res.data : [];

        if (!mounted) return;

        const mapped = list.map((item) => ({
          isoCode: item.isoCode,
          phoneCode: item.phoneCode,
          name: {
            kr: item.nameKr,
            en: item.nameEn,
            jp: item.nameJp,
          },
        }));

        setCountries(mapped);
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setCountries([]);
        setError(err);
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    fetchCountries();

    return () => {
      mounted = false;
    };
  }, []);

  return { countries, loading, error };
}
